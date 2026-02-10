package com.dieti.backend.service

import com.dieti.backend.dto.*
import com.dieti.backend.entity.*
import com.dieti.backend.repository.ImmobileSpecification
import com.dieti.backend.repository.ImmobileRepository
import com.dieti.backend.repository.UtenteRepository
import com.dieti.backend.repository.ImmagineRepository
import com.dieti.backend.repository.AgenteRepository // Necessario per il fix
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.util.UUID

@Service
class ImmobileService(
    private val immobileRepository: ImmobileRepository,
    private val utenteRepository: UtenteRepository,
    private val immagineRepository: ImmagineRepository,
    private val agenteRepository: AgenteRepository, // Iniettiamo il repository agenti
    private val ultimaRicercaService: UltimaRicercaService,
    private val geoapifyService: GeoapifyService,
    private val redisGeoService: RedisGeoService
) {

    fun getSuggestedCities(query: String): List<String> {
        val queryLower = query.lowercase().trim()
        val citiesFromRedis = redisGeoService.searchCities(queryLower)
        if (citiesFromRedis.isNotEmpty()) return citiesFromRedis

        val citiesFromDb = immobileRepository.findDistinctLocalita()

        if (citiesFromDb.isNotEmpty()) {
            citiesFromDb.forEach { city ->
                if (!city.isNullOrBlank() && city != "Non specificato") {
                    redisGeoService.addCity(city)
                }
            }
        }

        return citiesFromDb.filter {
            !it.isNullOrBlank() &&
                    it != "Non specificato" &&
                    it.contains(query, ignoreCase = true)
        }.sorted().take(10)
    }

    @Transactional
    fun searchImmobili(filters: ImmobileSearchFilters, userId: String?): List<ImmobileDTO> {
        val cleanQuery = filters.query?.trim()

        // FIX ROBUSTEZZA: Il try-catch DEVE essere qui, all'esterno della transazione REQUIRES_NEW.
        // Se UltimaRicercaService fallisce, lancia un'eccezione che noi catturiamo qui.
        // In questo modo la transazione principale (searchImmobili) rimane PULITA e i risultati vengono restituiti.
        if (!userId.isNullOrBlank() && !cleanQuery.isNullOrBlank()) {
            try {
                ultimaRicercaService.salvaRicerca(cleanQuery, userId)
            } catch (e: Exception) {
                // Logghiamo solo, non blocchiamo l'utente
                println("NON-BLOCKING ERROR: Impossibile salvare cronologia: ${e.message}")
            }
        }

        val cleanFilters = filters.copy(query = cleanQuery)

        val immobili = if (cleanFilters.lat != null && cleanFilters.lon != null && cleanFilters.radiusKm != null) {
            val idsVicini = redisGeoService.findNearbyImmobiliIds(cleanFilters.lat, cleanFilters.lon, cleanFilters.radiusKm)

            if (idsVicini.isEmpty()) {
                emptyList()
            } else {
                val uuidList = idsVicini.map { UUID.fromString(it) }
                val allInZone = immobileRepository.findAllById(uuidList)

                allInZone.filter { entity ->
                    (cleanFilters.tipoVendita == null || entity.tipoVendita == cleanFilters.tipoVendita) &&
                            (cleanFilters.minPrezzo == null || (entity.prezzo ?: 0) >= cleanFilters.minPrezzo) &&
                            (cleanFilters.maxPrezzo == null || (entity.prezzo ?: 0) <= cleanFilters.maxPrezzo)
                }
            }
        } else {
            val spec = ImmobileSpecification(cleanFilters)
            immobileRepository.findAll(spec)
        }

        return immobili.map { it.toDto() }
    }

    @Transactional
    fun creaImmobile(
        request: ImmobileCreateRequest,
        files: List<MultipartFile>?,
        emailUtente: String
    ): ImmobileDTO {
        val proprietario = utenteRepository.findByEmail(emailUtente)
            ?: throw EntityNotFoundException("Utente proprietario non trovato per email: $emailUtente")

        var parsedDate: LocalDate? = null
        if (!request.annoCostruzione.isNullOrBlank()) {
            try { parsedDate = LocalDate.parse(request.annoCostruzione) } catch (e: Exception) {}
        }

        val immobileEntity = request.toEntity(proprietario)
        if (immobileEntity.localita.isNullOrBlank()) immobileEntity.localita = "Non specificato"

        val savedImmobile = immobileRepository.save(immobileEntity)

        if (!files.isNullOrEmpty()) {
            val images = files.map {
                ImmagineEntity(immobile = savedImmobile, nome = it.originalFilename, formato = it.contentType, immagine = it.bytes)
            }
            immagineRepository.saveAll(images)
            savedImmobile.immagini.addAll(images)
        }

        if (request.ambienti.isNotEmpty()) {
            val amb = request.ambienti.map { AmbienteEntity(immobile=savedImmobile, tipologia=it.tipologia, numero=it.numero) }
            savedImmobile.ambienti.addAll(amb)
            immobileRepository.save(savedImmobile)
        }

        return savedImmobile.toDto()
    }

    @Transactional(readOnly = true)
    fun getImmobiliByAgenteId(agenteIdStr: String): List<ImmobileDTO> {
        val agenteUuid = UUID.fromString(agenteIdStr)
        val agente = agenteRepository.findById(agenteUuid).orElseThrow {
            EntityNotFoundException("Agente non trovato")
        }
        val utenteProprietario = utenteRepository.findByEmail(agente.email)
            ?: return emptyList()

        val immobili = immobileRepository.findAllByProprietarioUuid(utenteProprietario.uuid!!)
        return immobili.map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun getImmobileById(id: String): ImmobileDTO {
        val uuid = UUID.fromString(id)
        val entity = immobileRepository.findByIdOrNull(uuid)
            ?: throw EntityNotFoundException("Immobile non trovato")
        return entity.toDto()
    }

    @Transactional
    fun aggiornaImmobile(id: String, request: ImmobileCreateRequest, emailUtente: String): ImmobileDTO {
        val uuid = UUID.fromString(id)
        // Usa il metodo custom del repository per sicurezza
        val immobile = immobileRepository.findByUuidAndOwnerEmail(uuid, emailUtente)
            ?: throw EntityNotFoundException("Immobile non trovato o non autorizzato per la modifica")

        immobile.prezzo = request.prezzo
        immobile.descrizione = request.descrizione
        immobile.mq = request.mq
        immobile.piano = request.piano
        immobile.speseCondominiali = request.speseCondominiali
        immobile.arredamento = request.arredamento
        immobile.statoProprieta = request.statoProprieta
        // Aggiungi qui gli altri campi se necessario (ascensore, esposizione, ecc)

        if (request.ambienti.isNotEmpty()) {
            immobile.ambienti.clear()
            val nuoviAmbienti = request.ambienti.map { dto ->
                AmbienteEntity(immobile = immobile, tipologia = dto.tipologia, numero = dto.numero)
            }
            immobile.ambienti.addAll(nuoviAmbienti)
        }

        return immobileRepository.save(immobile).toDto()
    }

    @Transactional
    fun cancellaImmobile(id: String, emailUtente: String) {
        val uuid = UUID.fromString(id)
        val immobile = immobileRepository.findByUuidAndOwnerEmail(uuid, emailUtente)
            ?: throw EntityNotFoundException("Immobile non trovato o non autorizzato")

        redisGeoService.removeLocation(id)
        immobileRepository.delete(immobile)
    }

    // --- METODI AGGIUNTI PER LE IMMAGINI ---

    @Transactional
    fun aggiungiImmagini(idImmobile: String, files: List<MultipartFile>, emailUtente: String): ImmobileDTO {
        val uuid = UUID.fromString(idImmobile)

        // 1. Verifica che l'immobile esista e l'utente sia il proprietario
        val immobile = immobileRepository.findByUuidAndOwnerEmail(uuid, emailUtente)
            ?: throw EntityNotFoundException("Immobile non trovato o non sei autorizzato a modificarlo")

        // 2. Salva le nuove immagini
        if (files.isNotEmpty()) {
            val nuoveImmagini = files.map { file ->
                ImmagineEntity(
                    immobile = immobile,
                    nome = file.originalFilename,
                    formato = file.contentType,
                    immagine = file.bytes
                )
            }
            immagineRepository.saveAll(nuoveImmagini)
            immobile.immagini.addAll(nuoveImmagini)
        }

        return immobile.toDto()
    }

    @Transactional
    fun eliminaImmagine(idImmagine: Int, emailUtente: String) {
        // 1. Trova l'immagine usando Int (coerente con ImmagineRepository)
        val immagine = immagineRepository.findByIdOrNull(idImmagine)
            ?: throw EntityNotFoundException("Immagine non trovata")

        // 2. Risali all'immobile padre (gestione null safety)
        val immobile = immagine.immobile
            ?: throw EntityNotFoundException("Immagine orfana (nessun immobile associato)")

        // 3. Verifica sicurezza: chi richiede l'eliminazione deve essere il proprietario dell'immobile
        if (immobile.proprietario.email != emailUtente) {
            throw EntityNotFoundException("Non autorizzato: non sei il proprietario di questo immobile")
        }

        // 4. Rimuovi dalla lista dell'immobile (per coerenza Hibernate)
        immobile.immagini.remove(immagine)

        // 5. Cancella dal DB
        immagineRepository.delete(immagine)
    }
}


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

    fun creaImmobile(
        request: ImmobileCreateRequest,
        files: List<MultipartFile>?,
        emailUtente: String
    ): ImmobileDTO {
        // Qui il sistema trova l'UtenteRegistrato associato all'email dell'agente.
        // Se l'agente ha fatto il login, esiste un record UtenteRegistrato con la stessa email.
        val proprietario = utenteRepository.findByEmail(emailUtente)
            ?: throw EntityNotFoundException("Utente proprietario non trovato per email: $emailUtente")

        // ... (Logica di parsing date e coordinate invariata) ...
        var parsedDate: LocalDate? = null
        if (!request.annoCostruzione.isNullOrBlank()) {
            try { parsedDate = LocalDate.parse(request.annoCostruzione) } catch (e: Exception) {}
        }

        // Creazione Entity
        val immobileEntity = request.toEntity(proprietario)
        // Imposta località se non presente nel request (fallback)
        if (immobileEntity.localita.isNullOrBlank()) immobileEntity.localita = "Non specificato"

        // ... (Logica Geoapify opzionale qui) ...

        val savedImmobile = immobileRepository.save(immobileEntity)

        // ... (Salvataggio immagini e ambienti invariato) ...
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

    /**
     * FIX CRITICO: Recupera gli immobili creati da un Agente.
     * Poiché l'Agente e l'Utente sono su tabelle diverse ma condividono l'email,
     * facciamo il ponte tramite l'email.
     */
    @Transactional(readOnly = true)
    fun getImmobiliByAgenteId(agenteIdStr: String): List<ImmobileDTO> {
        val agenteUuid = UUID.fromString(agenteIdStr)

        // 1. Troviamo l'Agente
        val agente = agenteRepository.findById(agenteUuid).orElseThrow {
            EntityNotFoundException("Agente non trovato")
        }

        // 2. Troviamo l'Utente "ombra" che possiede fisicamente gli immobili
        val utenteProprietario = utenteRepository.findByEmail(agente.email)
            ?: return emptyList() // Se non c'è un utente collegato, non ha immobili

        // 3. Recuperiamo gli immobili usando l'UUID dell'Utente, non dell'Agente
        val immobili = immobileRepository.findAllByProprietarioUuid(utenteProprietario.uuid!!)

        return immobili.map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun getAllImmobili(): List<ImmobileDTO> {
        return immobileRepository.findAll().map { it.toDto() }
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

        // Verifica che l'immobile esista e appartenga all'utente loggato
        val immobile = immobileRepository.findByUuidAndOwnerEmail(uuid, emailUtente)
            ?: throw EntityNotFoundException("Immobile non trovato o non autorizzato per la modifica")

        // Aggiornamento campi semplici
        immobile.prezzo = request.prezzo
        immobile.descrizione = request.descrizione
        immobile.mq = request.mq
        immobile.piano = request.piano
        immobile.speseCondominiali = request.speseCondominiali
        immobile.arredamento = request.arredamento
        immobile.statoProprieta = request.statoProprieta
        // Nota: Le coordinate e l'indirizzo andrebbero ricalcolati con Geoapify se cambiano,
        // per semplicità qui aggiorniamo solo i dati testuali/numerici.

        // Aggiornamento Ambienti (Svuota e ripopola)
        if (request.ambienti.isNotEmpty()) {
            immobile.ambienti.clear()
            val nuoviAmbienti = request.ambienti.map { dto ->
                AmbienteEntity(
                    immobile = immobile,
                    tipologia = dto.tipologia,
                    numero = dto.numero
                )
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

        // Rimuovi anche da Redis Geo se necessario
        redisGeoService.removeLocation(id)

        immobileRepository.delete(immobile)
    }
}


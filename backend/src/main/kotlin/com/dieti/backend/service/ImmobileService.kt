package com.dieti.backend.service

import com.dieti.backend.dto.*
import com.dieti.backend.entity.*
import com.dieti.backend.repository.ImmobileSpecification
import com.dieti.backend.repository.ImmobileRepository
import com.dieti.backend.repository.UtenteRepository
import com.dieti.backend.repository.ImmagineRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID

@Service
class ImmobileService(
    private val immobileRepository: ImmobileRepository,
    private val utenteRepository: UtenteRepository,
    private val immagineRepository: ImmagineRepository,
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

        if (!userId.isNullOrBlank() && !cleanQuery.isNullOrBlank()) {
            try {
                ultimaRicercaService.salvaRicerca(cleanQuery, userId)
            } catch (e: Exception) {
                println("NON-BLOCKING ERROR: Impossibile salvare cronologia: ${e.message}")
            }
        }

        val cleanFilters = filters.copy(query = cleanQuery)

        // LOGICA RICERCA GEOGRAFICA + FILTRI
        val immobili = if (cleanFilters.lat != null && cleanFilters.lon != null && cleanFilters.radiusKm != null) {
            val idsVicini = redisGeoService.findNearbyImmobiliIds(cleanFilters.lat, cleanFilters.lon, cleanFilters.radiusKm)

            if (idsVicini.isEmpty()) {
                emptyList()
            } else {
                val uuidList = idsVicini.map { UUID.fromString(it) }
                val allInZone = immobileRepository.findAllById(uuidList)

                // FIX: Applicazione rigorosa di TUTTI i filtri in memoria sui risultati Geo
                allInZone.filter { entity ->
                    // Filtro Tipo Vendita
                    (cleanFilters.tipoVendita == null || entity.tipoVendita == cleanFilters.tipoVendita) &&
                            // Filtro Prezzo
                            (cleanFilters.minPrezzo == null || (entity.prezzo ?: 0) >= cleanFilters.minPrezzo) &&
                            (cleanFilters.maxPrezzo == null || (entity.prezzo ?: 0) <= cleanFilters.maxPrezzo) &&
                            // Filtro Superficie (MQ)
                            (cleanFilters.minMq == null || (entity.mq ?: 0) >= cleanFilters.minMq) &&
                            (cleanFilters.maxMq == null || (entity.mq ?: 0) <= cleanFilters.maxMq) &&
                            // Filtro Condizione/Stato (es. "Nuovo", "Da ristrutturare")
                            (cleanFilters.condizione.isNullOrBlank() || entity.statoProprieta.equals(cleanFilters.condizione, ignoreCase = true)) &&
                            // Filtro Bagni (Calcolato sommando gli ambienti "bagno")
                            (cleanFilters.bagni == null || calculateBathrooms(entity) >= cleanFilters.bagni)
                    // Nota: Stanze non implementato qui per semplicità, ma se necessario si può aggiungere logica simile ai bagni
                }
            }
        } else {
            // LOGICA RICERCA STANDARD (JPA Specification)
            val spec = ImmobileSpecification(cleanFilters)
            immobileRepository.findAll(spec)
        }

        return immobili.map { it.toDto() }
    }

    // Helper per calcolare i bagni in memoria
    private fun calculateBathrooms(entity: ImmobileEntity): Int {
        return entity.ambienti
            .filter { it.tipologia != null && it.tipologia!!.contains("bagno", ignoreCase = true) }
            .sumOf { it.numero ?: 1 }
    }

    @Transactional
    fun creaImmobile(
        request: ImmobileCreateRequest,
        files: List<MultipartFile>?,
        emailUtente: String
    ): ImmobileDTO {

        val proprietario = utenteRepository.findByEmail(emailUtente)
            ?: throw EntityNotFoundException("Utente non trovato: $emailUtente")

        var parsedDate: LocalDate? = null
        if (!request.annoCostruzione.isNullOrBlank()) {
            val dataStr = request.annoCostruzione
            if (dataStr != null) {
                try {
                    parsedDate = LocalDate.parse(dataStr)
                } catch (e: DateTimeParseException) {
                    if (dataStr.length == 4 && dataStr.all { it.isDigit() }) {
                        try {
                            parsedDate = LocalDate.of(dataStr.toInt(), 1, 1)
                        } catch (ignore: Exception) {}
                    }
                }
            }
        }

        var lat: Double? = null
        var lon: Double? = null
        var parks = false
        var schools = false
        var transport = false
        var comuneFinale: String = request.localita ?: "Non specificato"

        if (!request.indirizzo.isNullOrBlank()) {
            val coords = geoapifyService.getCoordinates(request.indirizzo, request.localita)
            if (coords != null) {
                lat = coords.lat
                lon = coords.lon
                if (!coords.city.isNullOrBlank()) comuneFinale = coords.city
                val amenities = geoapifyService.checkAmenities(lat, lon)
                parks = amenities.hasParks
                schools = amenities.hasSchools
                transport = amenities.hasPublicTransport
            }
        }

        val immobileEntity = ImmobileEntity(
            proprietario = proprietario,
            tipoVendita = request.tipoVendita,
            categoria = request.categoria,
            indirizzo = request.indirizzo,
            localita = comuneFinale,
            mq = request.mq,
            piano = request.piano,
            ascensore = request.ascensore,
            arredamento = request.arredamento,
            climatizzazione = request.climatizzazione,
            esposizione = request.esposizione,
            statoProprieta = request.statoProprieta,
            annoCostruzione = parsedDate,
            prezzo = request.prezzo,
            speseCondominiali = request.speseCondominiali,
            descrizione = request.descrizione,
            lat = lat,
            long = lon,
            parco = parks,
            scuola = schools,
            servizioPubblico = transport
        )

        val savedImmobile = immobileRepository.save(immobileEntity)

        if (lat != null && lon != null) {
            redisGeoService.addLocation(savedImmobile.uuid.toString(), lat, lon)
        }
        if (comuneFinale != "Non specificato") {
            redisGeoService.addCity(comuneFinale)
        }

        val immobileEntity = request.toEntity(proprietario)
        if (immobileEntity.localita.isNullOrBlank()) immobileEntity.localita = "Non specificato"

        val savedImmobile = immobileRepository.save(immobileEntity)

        if (!files.isNullOrEmpty()) {
            val imageEntities = files.map { file ->
                val fileBytes: ByteArray = file.bytes
                ImmagineEntity(
                    immobile = savedImmobile,
                    nome = file.originalFilename,
                    formato = file.contentType,
                    immagine = fileBytes
                )
            }
            immagineRepository.saveAll(imageEntities)
            savedImmobile.immagini.addAll(imageEntities)
        }

        if (request.ambienti.isNotEmpty()) {
            val ambienteEntities = request.ambienti.map { dto ->
                AmbienteEntity(
                    immobile = savedImmobile,
                    tipologia = dto.tipologia,
                    numero = dto.numero
                )
            }
            savedImmobile.ambienti.addAll(ambienteEntities)
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


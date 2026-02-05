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
}
package com.dieti.backend.controller

import com.dieti.backend.dto.ImmobileCreateRequest
import com.dieti.backend.dto.ImmobileDTO
import com.dieti.backend.dto.ImmobileSearchFilters
import com.dieti.backend.service.ImmobileService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/immobili")
class ImmobileController(
    private val immobileService: ImmobileService
) {

    /**
     * ENDPOINT MANCANTE AGGIUNTO
     * Restituisce i suggerimenti per l'autocompletamento dei comuni.
     */
    @GetMapping("/cities")
    fun getSuggestedCities(@RequestParam query: String): ResponseEntity<List<String>> {
        // Evita query troppo corte per non sovraccaricare
        if (query.length < 2) return ResponseEntity.ok(emptyList())

        val suggestions = immobileService.getSuggestedCities(query)
        return ResponseEntity.ok(suggestions)
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun creaImmobile(
        @RequestPart("immobile") immobileRequest: ImmobileCreateRequest,
        @RequestPart("immagini", required = false) immagini: List<MultipartFile>?,
        authentication: Authentication
    ): ResponseEntity<*> {
        return try {
            val emailUtente = authentication.name
            val nuovoImmobile = immobileService.creaImmobile(immobileRequest, immagini, emailUtente)
            ResponseEntity.status(HttpStatus.CREATED).body(nuovoImmobile)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore server: ${e.message}")
        }
    }

    @GetMapping
    fun getImmobili(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) tipoVendita: Boolean?,
        @RequestParam(required = false) minPrezzo: Int?,
        @RequestParam(required = false) maxPrezzo: Int?,
        @RequestParam(required = false) minMq: Int?,
        @RequestParam(required = false) maxMq: Int?,
        @RequestParam(required = false) bagni: Int?,
        @RequestParam(required = false) condizione: String?,
        // Parametri per ricerca Geo
        @RequestParam(required = false) lat: Double?,
        @RequestParam(required = false) lon: Double?,
        @RequestParam(required = false) radiusKm: Double?,
        authentication: Authentication?
    ): List<ImmobileDTO> {

        println("=== GET /api/immobili RICHIESTA RICEVUTA ===")
        println("Query params: q=$query, vendita=$tipoVendita")

        val filters = ImmobileSearchFilters(
            query = query,
            tipoVendita = tipoVendita,
            minPrezzo = minPrezzo,
            maxPrezzo = maxPrezzo,
            minMq = minMq,
            maxMq = maxMq,
            bagni = bagni,
            condizione = condizione,
            lat = lat,
            lon = lon,
            radiusKm = radiusKm
        )

        val userId = authentication?.name

        try {
            val result = immobileService.searchImmobili(filters, userId)
            println("=== SUCCESS: Restituisco ${result.size} immobili ===")
            return result
        } catch (e: Exception) {
            println("!!! CRASH DURANTE LA RICERCA !!!")
            e.printStackTrace()
            throw e
        }
    }

    @GetMapping("/{id}")
    fun getImmobile(@PathVariable id: String): ResponseEntity<ImmobileDTO> {
        return try {
            val immobile = immobileService.getImmobileById(id)
            ResponseEntity.ok(immobile)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }
}
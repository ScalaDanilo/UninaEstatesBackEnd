package com.dieti.backend.controller

import com.dieti.backend.dto.ImmobileCreateRequest
import com.dieti.backend.dto.ImmobileDTO
import com.dieti.backend.dto.ImmobileSearchFilters
import com.dieti.backend.service.ImmobileService
import jakarta.persistence.EntityNotFoundException
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
            val emailUtente = authentication.name // Qui arriva l'email dell'agente loggato
            val nuovoImmobile = immobileService.creaImmobile(immobileRequest, immagini, emailUtente)
            ResponseEntity.status(HttpStatus.CREATED).body(nuovoImmobile)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore server: ${e.message}")
        }
    }

    /**
     * Endpoint chiamato dalla schermata "I tuoi immobili" del Manager.
     */
    @GetMapping("/agente/{id}")
    fun getImmobiliByAgente(@PathVariable id: String): ResponseEntity<*> {
        return try {
            // Chiamiamo il metodo FIXATO che usa l'email come ponte
            val immobili = immobileService.getImmobiliByAgenteId(id)
            ResponseEntity.ok(immobili)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore: ${e.message}")
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

    @PutMapping("/{id}")
    fun updateImmobile(
        @PathVariable id: String,
        @RequestBody immobileRequest: ImmobileCreateRequest,
        authentication: Authentication
    ): ResponseEntity<*> {
        return try {
            val emailUtente = authentication.name
            val updated = immobileService.aggiornaImmobile(id, immobileRequest, emailUtente)
            ResponseEntity.ok(updated)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore aggiornamento: ${e.message}")
        }
    }

    @DeleteMapping("/{id}")
    fun deleteImmobile(
        @PathVariable id: String,
        authentication: Authentication
    ): ResponseEntity<*> {
        return try {
            val emailUtente = authentication.name
            immobileService.cancellaImmobile(id, emailUtente)
            ResponseEntity.ok("Immobile cancellato con successo")
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore cancellazione")
        }
    }

    // --- NUOVI ENDPOINT MANCANTI AGGIUNTI QUI ---

    @PostMapping(path = ["/{id}/immagini"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun aggiungiImmagini(
        @PathVariable id: String,
        @RequestPart("immagini") immagini: List<MultipartFile>,
        authentication: Authentication
    ): ResponseEntity<*> {
        return try {
            val emailUtente = authentication.name
            // Chiama il metodo che abbiamo aggiunto nel Service nel passaggio precedente
            val immobileAggiornato = immobileService.aggiungiImmagini(id, immagini, emailUtente)
            ResponseEntity.ok(immobileAggiornato)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore upload: ${e.message}")
        }
    }

    @DeleteMapping("/immagini/{imageId}")
    fun eliminaImmagine(
        @PathVariable imageId: Int,
        authentication: Authentication
    ): ResponseEntity<*> {
        return try {
            val emailUtente = authentication.name
            immobileService.eliminaImmagine(imageId, emailUtente)
            ResponseEntity.ok().build<Any>()
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore eliminazione: ${e.message}")
        }
    }
}
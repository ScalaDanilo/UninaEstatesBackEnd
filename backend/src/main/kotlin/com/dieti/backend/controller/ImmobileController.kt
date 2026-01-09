package com.dieti.backend.controller

import com.dieti.backend.ImmobileCreateRequest
import com.dieti.backend.ImmobileDTO
import com.dieti.backend.ImmobileSummaryDTO
import com.dieti.backend.service.ImmobileService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api")
class ImmobileController(
    private val immobileService: ImmobileService
) {

    // 1. CREA IMMOBILE (Multipart request: JSON + Files)
    @PostMapping("/immobili", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun creaImmobile(
        // @RequestPart converte la parte "immobile" del body da JSON all'oggetto DTO
        @RequestPart("immobile") immobileRequest: ImmobileCreateRequest,
        // @RequestPart gestisce la lista di file
        @RequestPart("immagini", required = false) immagini: List<MultipartFile>?,
        authentication: Authentication // Recupera l'utente loggato da Spring Security
    ): ResponseEntity<ImmobileDTO> {

        // Assumo che l'email o username sia nel principal
        val emailUtente = authentication.name

        val nuovoImmobile = immobileService.creaImmobile(immobileRequest, immagini, emailUtente)
        return ResponseEntity.status(HttpStatus.CREATED).body(nuovoImmobile)
    }

    // 2. GET TUTTI GLI IMMOBILI
    @GetMapping("/immobili")
    fun getAllImmobili(): ResponseEntity<List<ImmobileSummaryDTO>> {
        val immobili = immobileService.getAllImmobili()
        return ResponseEntity.ok(immobili)
    }

    // 3. GET SINGOLO IMMOBILE (Dettaglio)
    @GetMapping("/immobili/{id}")
    fun getImmobile(@PathVariable id: String): ResponseEntity<ImmobileDTO> {
        val immobile = immobileService.getImmobileById(id)
        return ResponseEntity.ok(immobile)
    }

    // 4. GET IMMAGINE RAW (Usato dal browser per visualizzare la foto)
    // URL corrisponde a quello generato nel tuo toDto(): /api/immagini/{id}/raw
    @GetMapping("/immagini/{id}/raw")
    fun getImmagineRaw(@PathVariable id: Int): ResponseEntity<ByteArray> {
        val immagineEntity = immobileService.getImmagineContent(id)

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(immagineEntity.formato ?: "image/jpeg"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${immagineEntity.nome}\"")
            .body(immagineEntity.immagine)
    }
}
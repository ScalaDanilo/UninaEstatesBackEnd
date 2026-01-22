package com.dieti.backend.controller

import com.dieti.backend.dto.ImmobileCreateRequest
import com.dieti.backend.dto.ImmobileDTO
import com.dieti.backend.service.ImmobileService
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.PrintWriter
import java.io.StringWriter

@RestController
@RequestMapping("/api/immobili")
class ImmobileController(
    private val immobileService: ImmobileService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun creaImmobile(
        @RequestPart("immobile") immobileRequest: ImmobileCreateRequest,
        @RequestPart("immagini", required = false) immagini: List<MultipartFile>?,
        authentication: Authentication
    ): ResponseEntity<*> {
        return try {
            val emailUtente = authentication.name
            println("=== RICHIESTA CREAZIONE IMMOBILE ===")
            println("Utente: $emailUtente")
            println("Dati: $immobileRequest")
            println("Immagini ricevute: ${immagini?.size ?: 0}")

            if (immagini != null && immagini.isNotEmpty()) {
                println("Info primo file: ${immagini[0].originalFilename} (${immagini[0].size} bytes)")
            }

            val nuovoImmobile = immobileService.creaImmobile(immobileRequest, immagini, emailUtente)

            println("=== SUCCESSO: Immobile creato con ID ${nuovoImmobile.id} ===")
            ResponseEntity.status(HttpStatus.CREATED).body(nuovoImmobile)

        } catch (e: EntityNotFoundException) {
            println("ERRORE UTENTE: ${e.message}")
            ResponseEntity.status(HttpStatus.FORBIDDEN).body("Utente non trovato.")
        } catch (e: Exception) {
            println("!!! ERRORE CRITICO (500) !!!")
            e.printStackTrace() // Stampa l'errore completo nella console del server

            // Restituisce l'errore dettagliato al client (utile per debug)
            val sw = StringWriter()
            e.printStackTrace(PrintWriter(sw))
            val errorDetails = "Errore Server: ${e.message}\n\nStack:\n$sw"

            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails)
        }
    }

    @GetMapping
    fun getImmobili(): List<ImmobileDTO> {
        return immobileService.getAllImmobili()
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
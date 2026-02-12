package com.dieti.backend.controller

import com.dieti.backend.dto.toDTO
import com.dieti.backend.service.AgenteService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.Collections
import java.util.UUID

@RestController
@RequestMapping("/api/agenti")
@CrossOrigin(origins = ["*"])
class AgenteController(
    private val agenteService: AgenteService
) {
    private val logger = LoggerFactory.getLogger(AgenteController::class.java)

    @GetMapping("/{id}")
    fun getAgenteProfile(@PathVariable id: String): ResponseEntity<Any> {
        return try {
            val uuid = UUID.fromString(id)
            val agente = agenteService.getAgenteById(uuid)
            ResponseEntity.ok(agente.toDTO())
        } catch (e: Exception) {
            ResponseEntity.status(404).body("Agente non trovato: ${e.message}")
        }
    }

    // --- ENDPOINT MANAGER ---

    @GetMapping("/richieste")
    fun getRichiestePendenti(authentication: Authentication): ResponseEntity<*> {
        return try {
            val email = authentication.name
            val richieste = agenteService.getRichiestePendenti(email)
            ResponseEntity.ok(richieste)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Errore: ${e.message}")
        }
    }

    @PostMapping("/richieste/{id}/accetta")
    fun accettaRichiesta(@PathVariable id: String, authentication: Authentication): ResponseEntity<*> {
        return try {
            val email = authentication.name
            agenteService.accettaIncarico(email, id)
            // MODIFICA: Restituiamo una mappa (JSON) invece di una stringa semplice
            val response = Collections.singletonMap("message", "Incarico accettato con successo")
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(Collections.singletonMap("error", "Errore: ${e.message}"))
        }
    }

    @PostMapping("/richieste/{id}/rifiuta")
    fun rifiutaRichiesta(@PathVariable id: String, authentication: Authentication): ResponseEntity<*> {
        return try {
            val email = authentication.name
            agenteService.rifiutaIncarico(email, id)
            // MODIFICA: Restituiamo una mappa (JSON)
            val response = Collections.singletonMap("message", "Incarico rifiutato e immobile rimosso")
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(Collections.singletonMap("error", "Errore: ${e.message}"))
        }
    }
}
package com.dieti.backend.controller

import com.dieti.backend.dto.toDto
import com.dieti.backend.service.UtenteService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/utenti")
@CrossOrigin(origins = ["*"]) // Configura come necessario per la sicurezza
class UtenteController(
    private val utenteService: UtenteService
) {

    @GetMapping("/{id}")
    fun getUserProfile(@PathVariable id: UUID): ResponseEntity<Any> {
        return try {
            val utente = utenteService.getUtenteById(id)
            ResponseEntity.ok(utente.toDto())
        } catch (e: Exception) {
            ResponseEntity.status(404).body("Utente non trovato")
        }
    }
}
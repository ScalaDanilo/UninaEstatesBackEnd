package com.dieti.backend.controller

import com.dieti.backend.dto.LoginRequest
import com.dieti.backend.dto.UtenteRegistrazioneRequest
import com.dieti.backend.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    // Registrazione (Lasciamo semplice)
    @PostMapping("/register")
    fun register(@RequestBody request: UtenteRegistrazioneRequest): ResponseEntity<Any> {
        return try {
            val nuovoUtente = authService.registraUtente(request)
            ResponseEntity.ok(nuovoUtente)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }

    // Login (Gestito con Try-Catch per evitare il 500)
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        return try {
            println("Tentativo login per: ${request.email}") // Debug Log

            val utenteLoggato = authService.login(request)

            // Se arrivo qui, è andato tutto bene
            ResponseEntity.ok(utenteLoggato)

        } catch (e: RuntimeException) {
            // Se AuthService lancia "Password errata" o "Utente non trovato"
            println("Errore Login: ${e.message}") // Debug Log

            // Restituisco 401 Unauthorized invece di 500
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenziali non valide: ${e.message}")
        }
    }
}
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

    // Registrazione
    @PostMapping("/register")
    fun register(@RequestBody request: UtenteRegistrazioneRequest): ResponseEntity<Any> {
        return try {
            val nuovoUtente = authService.registraUtente(request)
            ResponseEntity.ok(nuovoUtente)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }

    // Login
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        return try {
            println("Tentativo login per: ${request.email}")

            val utenteLoggato = authService.login(request)

            ResponseEntity.ok(utenteLoggato)

        } catch (e: RuntimeException) {
            println("Errore Login: ${e.message}")

            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenziali non valide: ${e.message}")
        }
    }
}
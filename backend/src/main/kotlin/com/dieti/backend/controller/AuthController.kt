package com.dieti.backend.controller

import com.dieti.backend.dto.*
import com.dieti.backend.entity.UtenteRegistratoEntity
import com.dieti.backend.repository.UtenteRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val utenteRepository: UtenteRepository
) {

    // 1. Login Classico
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        val user = utenteRepository.findByEmail(request.email)

        if (user != null && user.password == request.password) {
            return ResponseEntity.ok(
                AuthResponse(
                    token = UUID.randomUUID().toString(),
                    userId = user.uuid!!,
                    nome = user.nome,
                    email = user.email,
                    ruolo = "UTENTE"
                )
            )
        }
        return ResponseEntity.status(401).body("Credenziali non valide")
    }

    // 2. Registrazione
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<Any> {
        if (utenteRepository.findByEmail(request.email) != null) {
            return ResponseEntity.badRequest().body("Email già in uso")
        }

        val newUser = UtenteRegistratoEntity(
            nome = request.nome,
            cognome = request.cognome,
            email = request.email,
            password = request.password,
            telefono = request.telefono
        )

        val savedUser = utenteRepository.save(newUser)

        return ResponseEntity.ok(
            AuthResponse(
                token = UUID.randomUUID().toString(),
                userId = savedUser.uuid!!,
                nome = savedUser.nome,
                email = savedUser.email,
                ruolo = "UTENTE"
            )
        )
    }

    // 3. Login con Google
    @PostMapping("/google")
    fun googleLogin(@RequestBody request: GoogleLoginRequest): ResponseEntity<Any> {
        var user = utenteRepository.findByEmail(request.email)

        if (user == null) {
            user = UtenteRegistratoEntity(
                nome = request.firstName ?: "Utente",
                cognome = request.lastName ?: "Google",
                email = request.email,
                password = "GOOGLE_USER_NO_PASSWORD",
                telefono = null
            )
            utenteRepository.save(user)
        }

        // FIX: Rimosso '!!' inutile dopo 'user'.
        // Kotlin sa già che 'user' non è null qui.
        // Manteniamo solo '!!' su 'uuid' perché quello è nullable nell'Entity.
        return ResponseEntity.ok(
            AuthResponse(
                token = UUID.randomUUID().toString(),
                userId = user.uuid!!,
                nome = user.nome,
                email = user.email,
                ruolo = "UTENTE"
            )
        )
    }
}
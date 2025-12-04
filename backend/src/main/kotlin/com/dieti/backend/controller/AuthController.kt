package com.dieti.backend.controller

import com.dieti.backend.dto.AuthResponse
import com.dieti.backend.dto.GoogleLoginRequest
import com.dieti.backend.dto.LoginRequest
import com.dieti.backend.dto.RegisterRequest
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

        // Controllo password molto semplice (in prod usa BCrypt!)
        if (user != null && user.password == request.password) {
            return ResponseEntity.ok(
                AuthResponse(
                    token = UUID.randomUUID().toString(), // Token fittizio
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
            password = request.password, // Ricorda di hashare in futuro
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
        // Qui dovresti verificare il token Google con le API di Google
        // Per ora simuliamo: se l'email esiste, login. Se no, lo registriamo al volo.

        var user = utenteRepository.findByEmail(request.email)

        if (user == null) {
            // Registrazione automatica da Google
            user = UtenteRegistratoEntity(
                nome = request.firstName ?: "Utente",
                cognome = request.lastName ?: "Google",
                email = request.email,
                password = null, // Password null per utenti social
                telefono = null
            )
            utenteRepository.save(user)
        }

        return ResponseEntity.ok(
            AuthResponse(
                token = UUID.randomUUID().toString(),
                userId = user!!.uuid!!,
                nome = user.nome,
                email = user.email,
                ruolo = "UTENTE"
            )
        )
    }
}
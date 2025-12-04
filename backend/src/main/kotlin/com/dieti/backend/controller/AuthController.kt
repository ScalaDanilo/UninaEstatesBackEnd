package com.dieti.backend.controller

import com.dieti.backend.dto.GoogleLoginRequest
import com.dieti.backend.entity.UtenteRegistratoEntity
import com.dieti.backend.repository.UtenteRepository
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.Collections

@RestController
@RequestMapping("/api/auth")
class AuthController(val utenteRepo: UtenteRepository) {

    // IMPORTANTE: Questo ID lo trovi nella Google Cloud Console (dove hai configurato Android)
    // Cerca "Web Client ID" (anche se usi Android, per il backend serve quello Web)
    private val GOOGLE_CLIENT_ID = "IL_TUO_CLIENT_ID_DI_GOOGLE.apps.googleusercontent.com"

    @PostMapping("/google")
    fun googleLogin(@RequestBody request: GoogleLoginRequest): ResponseEntity<Any> {
        try {
            // 1. Configura il verificatore di Google
            val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build()

            // 2. Verifica il token ricevuto da Android
            val idToken = verifier.verify(request.idToken)
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token Google non valido")

            // 3. Estrai i dati dell'utente dal token sicuro
            val payload = idToken.payload
            val email = payload.email
            val nome = payload["given_name"] as String? ?: "Utente"
            val cognome = payload["family_name"] as String? ?: "Google"

            // 4. Controlla se l'utente esiste già nel DB
            var utente = utenteRepo.findByEmail(email)

            if (utente == null) {
                // 5. Se non esiste, REGISTRALO automaticamente (senza password)
                val nuovoUtente = UtenteRegistratoEntity(
                    nome = nome,
                    cognome = cognome,
                    email = email,
                    password = null, // Utente Google -> niente password
                    telefono = null
                )
                utente = utenteRepo.save(nuovoUtente)
            }

            // 6. Restituisci l'UUID dell'utente all'app Android
            // (L'app lo userà per le future richieste, es. caricare immobili)
            return ResponseEntity.ok(mapOf(
                "message" to "Login effettuato",
                "userId" to utente.uuid,
                "nome" to utente.nome
            ))

        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore login: ${e.message}")
        }
    }
}
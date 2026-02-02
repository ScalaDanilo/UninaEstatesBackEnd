package com.dieti.backend.controller

import com.dieti.backend.dto.toDto
import com.dieti.backend.service.UtenteService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/utenti")
@CrossOrigin(origins = ["*"])
class UtenteController(
    private val utenteService: UtenteService
) {

    @GetMapping("/{id}")
    fun getUserProfile(@PathVariable id: String): ResponseEntity<Any> {
        // STAMPA DI DEBUG (Funziona senza librerie)
        println("--------------------------------------------------")
        println(">>> CONTROLLER: Richiesto profilo per ID stringa: $id")

        return try {
            // 1. Proviamo a convertire la stringa in UUID
            val uuid = UUID.fromString(id)
            println(">>> CONTROLLER: Conversione UUID riuscita: $uuid")

            // 2. Chiediamo al service
            val utente = utenteService.getUtenteById(uuid)
            println(">>> CONTROLLER: Utente trovato nel Service! Nome: ${utente.nome}")

            // 3. Restituiamo il DTO
            ResponseEntity.ok(utente.toDto())

        } catch (e: IllegalArgumentException) {
            println(">>> CONTROLLER: Errore conversione UUID. L'ID non è valido.")
            ResponseEntity.badRequest().body("ID non valido: $id")
        } catch (e: Exception) {
            println(">>> CONTROLLER: Eccezione generica o Utente non trovato.")
            e.printStackTrace() // Stampa l'errore completo in console
            ResponseEntity.status(404).body("Utente non trovato: ${e.message}")
        }
    }
}
package com.dieti.backend.controller

import com.dieti.backend.dto.toDTO
import com.dieti.backend.service.AgenteService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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
        logger.info(">>> CONTROLLER: Richiesto profilo agente per ID stringa: {}", id)

        return try {
            // 1. Conversione sicura da String a UUID
            val uuid = UUID.fromString(id)

            // 2. Recupero Agente dal Service (Restituisce AgenteEntity)
            // Assicurati che AgenteService abbia il metodo getAgenteById(UUID)
            val agente = agenteService.getAgenteById(uuid)

            // 3. Conversione Entity -> DTO e risposta
            ResponseEntity.ok(agente.toDTO())

        } catch (e: IllegalArgumentException) {
            // Gestione ID malformato
            logger.warn("ID Agente non valido: {}", id)
            ResponseEntity.badRequest().body("ID Agente non valido")
        } catch (e: Exception) {
            // Gestione Agente non trovato o altri errori
            logger.error("Errore recupero agente", e)
            ResponseEntity.status(404).body("Agente non trovato: ${e.message}")
        }
    }
}
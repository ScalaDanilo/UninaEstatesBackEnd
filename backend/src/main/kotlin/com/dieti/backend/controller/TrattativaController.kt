package com.dieti.backend.controller

import com.dieti.backend.dto.StoriaTrattativaDTO
import com.dieti.backend.dto.TrattativaSummaryDTO
import com.dieti.backend.dto.UserResponseRequest
import com.dieti.backend.service.RispostaService
import com.dieti.backend.service.TrattativaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.Collections

@RestController
@RequestMapping("/api/trattative")
@CrossOrigin(origins = ["*"])
class TrattativaController(
    private val trattativaService: TrattativaService,
    private val rispostaService: RispostaService
) {

    @GetMapping("/utente/{userId}")
    fun getTrattativeUtente(@PathVariable userId: String): ResponseEntity<List<TrattativaSummaryDTO>> {
        return try {
            val result = trattativaService.getTrattativeUtente(userId)
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.badRequest().build()
        }
    }

    // NUOVO ENDPOINT PER IL MANAGER
    @GetMapping("/manager/{agenteId}")
    fun getTrattativeManager(@PathVariable agenteId: String): ResponseEntity<List<TrattativaSummaryDTO>> {
        return try {
            val result = trattativaService.getTrattativeManager(agenteId)
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/{offertaId}/storia")
    fun getStoriaTrattativa(
        @PathVariable offertaId: String,
        @RequestParam viewerId: String
    ): ResponseEntity<StoriaTrattativaDTO> {
        return try {
            val result = trattativaService.getStoriaTrattativa(offertaId, viewerId)
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/utente/rispondi")
    fun rispondiUtente(@RequestBody request: UserResponseRequest): ResponseEntity<Map<String, String?>> {
        return try {
            rispostaService.creaRispostaUtente(request)
            ResponseEntity.ok(mapOf("message" to "Risposta inviata con successo"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
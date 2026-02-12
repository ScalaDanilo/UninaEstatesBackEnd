package com.dieti.backend.controller

import com.dieti.backend.dto.EsitoRichiestaRequest
import com.dieti.backend.dto.RichiestaDTO
import com.dieti.backend.service.GestioneImmobiliService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/manager/richieste")
@CrossOrigin(origins = ["*"])
class GestioneImmobiliController(
    private val gestioneImmobiliService: GestioneImmobiliService
) {

    /**
     * ENDPOINT: Recupera gli immobili della stessa agenzia del manager che non hanno ancora un agente assegnato.
     */
    @GetMapping("/{agenteId}/pendenti")
    fun getRichiestePendenti(@PathVariable agenteId: String): ResponseEntity<List<RichiestaDTO>> {
        return try {
            val richieste = gestioneImmobiliService.getImmobiliDaApprovare(agenteId)
            ResponseEntity.ok(richieste)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * ENDPOINT: Il manager accetta l'immobile.
     * Azione DB: UPDATE immobile SET agente_id = [manager_id] WHERE id = [immobile_id]
     */
    @PostMapping("/accetta")
    fun accettaRichiesta(
        @RequestBody request: EsitoRichiestaRequest,
        @RequestHeader("X-Manager-Id") managerId: String // Usiamo l'header per sapere chi sta accettando
    ): ResponseEntity<String> {
        return try {
            gestioneImmobiliService.accettaImmobile(request.id, managerId)
            ResponseEntity.ok("Immobile preso in carico con successo")
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.badRequest().body("Errore: ${e.message}")
        }
    }

    /**
     * ENDPOINT: Il manager rifiuta l'immobile.
     * Azione DB: DELETE FROM immobile WHERE id = [immobile_id]
     */
    @PostMapping("/rifiuta")
    fun rifiutaRichiesta(@RequestBody request: EsitoRichiestaRequest): ResponseEntity<String> {
        return try {
            gestioneImmobiliService.rifiutaImmobile(request.id)
            ResponseEntity.ok("Immobile rifiutato e rimosso")
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.badRequest().body("Errore: ${e.message}")
        }
    }
}
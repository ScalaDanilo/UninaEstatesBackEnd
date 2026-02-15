package com.dieti.backend.controller

import com.dieti.backend.dto.EsitoRichiestaRequest
import com.dieti.backend.dto.ManagerActionResponse // Importa il nuovo DTO
import com.dieti.backend.dto.RichiestaDTO
import com.dieti.backend.service.GestioneImmobiliService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.Collections

@RestController
@RequestMapping("/api/manager/richieste")
@CrossOrigin(origins = ["*"])
class GestioneImmobiliController(
    private val gestioneImmobiliService: GestioneImmobiliService
) {

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

    // FIX: Restituiamo ManagerActionResponse e specifichiamo produces JSON per evitare errori di parsing
    @PostMapping(value = ["/accetta"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun accettaRichiesta(
        @RequestBody request: EsitoRichiestaRequest,
        @RequestHeader("X-Manager-Id") managerId: String
    ): ResponseEntity<ManagerActionResponse> {
        return try {
            gestioneImmobiliService.accettaImmobile(request.id, managerId)

            // Risposta tipizzata
            ResponseEntity.ok(ManagerActionResponse(message = "Immobile preso in carico con successo"))
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.badRequest().body(ManagerActionResponse(error = e.message ?: "Errore generico"))
        }
    }

    @PostMapping(value = ["/rifiuta"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun rifiutaRichiesta(@RequestBody request: EsitoRichiestaRequest): ResponseEntity<ManagerActionResponse> {
        return try {
            gestioneImmobiliService.rifiutaImmobile(request.id)

            // Risposta tipizzata
            ResponseEntity.ok(ManagerActionResponse(message = "Immobile rifiutato e rimosso"))
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.badRequest().body(ManagerActionResponse(error = e.message ?: "Errore generico"))
        }
    }
}
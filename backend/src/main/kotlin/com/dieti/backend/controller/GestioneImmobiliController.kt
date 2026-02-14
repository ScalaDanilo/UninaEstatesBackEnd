package com.dieti.backend.controller

import com.dieti.backend.dto.EsitoRichiestaRequest
import com.dieti.backend.dto.RichiestaDTO
import com.dieti.backend.service.GestioneImmobiliService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.Collections

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
     * FIX: Restituisce un JSON { "message": "..." } invece di una stringa raw per evitare errori Retrofit.
     */
    @PostMapping("/accetta")
    fun accettaRichiesta(
        @RequestBody request: EsitoRichiestaRequest,
        @RequestHeader("X-Manager-Id") managerId: String // Usiamo l'header per sapere chi sta accettando
    ): ResponseEntity<Map<String, String>> {
        return try {
            gestioneImmobiliService.accettaImmobile(request.id, managerId)

            // Restituiamo una Mappa che verrà serializzata in JSON corretto
            val response = Collections.singletonMap("message", "Immobile preso in carico con successo")
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            e.printStackTrace()
            val errorResponse = Collections.singletonMap("error", e.message ?: "Errore generico")
            ResponseEntity.badRequest().body(errorResponse)
        }
    }

    /**
     * ENDPOINT: Il manager rifiuta l'immobile.
     * FIX: Uniformato per restituire JSON { "message": "..." } come per l'accettazione.
     */
    @PostMapping("/rifiuta")
    fun rifiutaRichiesta(@RequestBody request: EsitoRichiestaRequest): ResponseEntity<Map<String, String>> {
        return try {
            gestioneImmobiliService.rifiutaImmobile(request.id)

            val response = Collections.singletonMap("message", "Immobile rifiutato e rimosso")
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            e.printStackTrace()
            val errorResponse = Collections.singletonMap("error", e.message ?: "Errore generico")
            ResponseEntity.badRequest().body(errorResponse)
        }
    }
}
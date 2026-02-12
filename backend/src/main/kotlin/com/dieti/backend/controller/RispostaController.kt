package com.dieti.backend.controller

import com.dieti.backend.dto.RispostaRequest
import com.dieti.backend.service.RispostaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/risposte")
@CrossOrigin(origins = ["*"])
class RispostaController(
    private val rispostaService: RispostaService
) {

    @PostMapping
    fun inviaRisposta(@RequestBody request: RispostaRequest): ResponseEntity<Map<String, String>> {
        return try {
            // Chiamiamo il metodo specifico per il manager
            rispostaService.creaRispostaManager(request)

            val messaggioSuccesso = when (request.esito) {
                "ACCETTATA" -> "Offerta accettata con successo. Utente notificato."
                "RIFIUTATA" -> "Offerta rifiutata. Utente notificato."
                "CONTROPROPOSTA" -> "Controproposta inviata."
                else -> "Risposta elaborata."
            }

            // Usa mapOf("chiave" to "valore") invece di Collections.singletonMap
            ResponseEntity.ok(mapOf("message" to messaggioSuccesso))

        } catch (e: Exception) {
            // Gestione sicura: se e.message è null, usa una stringa di default
            val errore = e.message ?: "Errore generico durante l'operazione del manager"
            ResponseEntity.badRequest().body(mapOf("error" to errore))
        }
    }
}
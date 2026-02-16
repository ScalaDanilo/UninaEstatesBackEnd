package com.dieti.backend.controller

import com.dieti.backend.dto.OffertaRequest
import com.dieti.backend.dto.OffertaRicevutaDTO
import com.dieti.backend.service.OffertaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.Collections

@RestController
@RequestMapping("/api/offerte")
@CrossOrigin(origins = ["*"])
class OffertaController(
    private val offertaService: OffertaService
) {

    @PostMapping
    fun inviaOfferta(@RequestBody request: OffertaRequest): ResponseEntity<Map<String, String>> {
        return try {
            offertaService.creaOfferta(request)
            ResponseEntity.ok(Collections.singletonMap("message", "Offerta inviata con successo"))
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.badRequest().body(Collections.singletonMap("error", "Errore: ${e.message}"))
        }
    }

    @GetMapping("/pendenti/{agenteId}")
    fun getOffertePendenti(@PathVariable agenteId: String): ResponseEntity<List<OffertaRicevutaDTO>> {
        return try {
            val offerte = offertaService.getOffertePendentiPerAgente(agenteId)
            ResponseEntity.ok(offerte)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.ok(emptyList())
        }
    }
}
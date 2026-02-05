package com.dieti.backend.controller

import com.dieti.backend.service.UltimaRicercaService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ricerche")
class RicercaController(
    private val ultimaRicercaService: UltimaRicercaService
) {

    @GetMapping
    fun getRicercheRecenti(authentication: Authentication): ResponseEntity<List<String>> {
        val email = authentication.name
        val ricerche = ultimaRicercaService.getRicercheRecenti(email)
        return ResponseEntity.ok(ricerche)
    }

    @DeleteMapping
    fun cancellaRicerca(
        @RequestParam query: String,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val email = authentication.name
        ultimaRicercaService.cancellaRicerca(query, email)
        return ResponseEntity.noContent().build()
    }
}
package com.dieti.backend.controller

import com.dieti.backend.dto.*
import com.dieti.backend.service.AgenteService
import com.dieti.backend.service.AgenziaService
import com.dieti.backend.service.AmministratoreService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = ["*"])
class AmministratoreController(
    private val amministratoreService: AmministratoreService,
    private val agenteService: AgenteService,
    private val agenziaService: AgenziaService
) {
    // ... (login, createAdmin esistenti) ...
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<*> { /* ... come prima ... */ return ResponseEntity.ok("Login OK") } // (Semplificato per brevità, usa il tuo codice precedente)

    @PostMapping("/create-admin")
    fun createAdmin(@RequestBody request: CreateAdminRequest): ResponseEntity<*> {
        return try {
            val newAdmin = amministratoreService.creaAmministratore(request)
            ResponseEntity.ok(newAdmin)
        } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }
    }

    // --- NUOVI ENDPOINT ---

    @GetMapping("/administrators-options")
    fun getAdministratorsOptions(): ResponseEntity<List<AdminOptionDTO>> {
        return ResponseEntity.ok(amministratoreService.getAdministratorsOptions())
    }

    @PostMapping("/change-my-password")
    fun changeMyPassword(
        @RequestBody request: ChangeMyPasswordRequest,
        authentication: Authentication // Spring Security inietta l'utente autenticato dal filtro
    ): ResponseEntity<*> {
        return try {
            val email = authentication.name // L'email dell'admin loggato
            amministratoreService.cambiaPasswordPersonale(email, request)
            ResponseEntity.ok("Password aggiornata con successo")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    // ... (createAgency, createAgent, getAgenciesOptions esistenti) ...
    @PostMapping("/create-agency")
    fun createAgency(@RequestBody request: CreateAgenziaRequest): ResponseEntity<*> {
        return try {
            val agenzia = agenziaService.creaAgenzia(request)
            ResponseEntity.ok(agenzia)
        } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }
    }

    @GetMapping("/agencies-options")
    fun getAgenciesOptions(): ResponseEntity<List<AgenziaOptionDTO>> {
        return ResponseEntity.ok(agenziaService.getAgenzieOptionsForAdmin())
    }

    @PostMapping("/create-agent")
    fun createAgent(@RequestBody request: CreateAgenteRequest): ResponseEntity<*> {
        return try {
            val agente = agenteService.creaAgente(request)
            ResponseEntity.ok(agente)
        } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }
    }
}
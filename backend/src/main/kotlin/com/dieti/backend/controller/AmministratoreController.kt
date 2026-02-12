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
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<*> {
        return try {
            val admin = amministratoreService.login(request)
            ResponseEntity.ok(AdminLoginResponse(admin.uuid.toString(), admin.email, "ADMIN"))
        } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }
    }

    @PostMapping("/create-admin")
    fun createAdmin(@RequestBody request: CreateAdminRequest): ResponseEntity<*> {
        return try {
            val newAdmin = amministratoreService.creaAmministratore(request)
            ResponseEntity.ok(newAdmin)
        } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }
    }

    @GetMapping("/administrators-options")
    fun getAdministratorsOptions(): ResponseEntity<List<AdminOptionDTO>> {
        return ResponseEntity.ok(amministratoreService.getAdministratorsOptions())
    }

    @PostMapping("/change-my-password")
    fun changeMyPassword(
        @RequestBody request: ChangeMyPasswordRequest,
        authentication: Authentication
    ): ResponseEntity<*> {
        return try {
            val email = authentication.name
            amministratoreService.cambiaPasswordPersonale(email, request)
            ResponseEntity.ok("Password aggiornata con successo")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    // --- GESTIONE AGENZIE E AGENTI ---

    @PostMapping("/create-agency")
    fun createAgency(@RequestBody request: CreateAgenziaRequest): ResponseEntity<*> {
        return try {
            // Ora questo chiama correttamente il metodo implementato in AgenziaService
            val agenzia = agenziaService.creaAgenzia(request)
            ResponseEntity.ok(agenzia)
        } catch (e: Exception) { ResponseEntity.badRequest().body(e.message) }
    }

    @GetMapping("/agencies-options")
    fun getAgenciesOptions(): ResponseEntity<List<AgenziaOptionDTO>> {
        // Ora questo chiama correttamente il metodo implementato in AgenziaService
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
package com.dieti.backend.controller

import com.dieti.backend.entity.AgenteEntity
import com.dieti.backend.service.AgenteService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = ["*"])
class AgenziaController(
    private val agenteService: AgenteService // Corretto anche un piccolo typo nel nome variabile (era agenteSerivce)
) {

    @GetMapping("/agenti")
    fun getAllAgenti(): List<AgenteEntity> {
        // FIX: Il metodo nel service ora si chiama 'getAllAgenti'
        return agenteService.getAllAgenti()
    }
}
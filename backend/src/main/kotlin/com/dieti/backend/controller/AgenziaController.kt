package com.dieti.backend.controller

import com.dieti.backend.entity.AgenteEntity
import com.dieti.backend.service.AgenteService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = ["*"])
class AgenziaController(
    private val agenteSerivce: AgenteService
) {


    @GetMapping("/agenti")
    fun getAllAgenti(): List<AgenteEntity> {
        return agenteSerivce.prendiTuttiGliAgenti()
    }
}
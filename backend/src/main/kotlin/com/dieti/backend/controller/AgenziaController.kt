package com.dieti.backend.controller

import com.dieti.backend.dto.AgenziaDTO
import com.dieti.backend.entity.AgenteEntity
import com.dieti.backend.repository.AgenziaRepository
import com.dieti.backend.service.AgenteService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = ["*"])
class AgenziaController(
    private val agenziaRepository: AgenziaRepository,
    private val agenteSerivce: AgenteService
) {

    @GetMapping("/agenzie")
    fun getAllAgenzie(): List<AgenziaDTO> {
        return agenziaRepository.findAll().map {
            AgenziaDTO(nome = it.nome)
        }
    }

    @GetMapping("/agenti")
    fun getAllAgenti(): List<AgenteEntity> {
        return agenteSerivce.prendiTuttiGliAgenti()
    }
}
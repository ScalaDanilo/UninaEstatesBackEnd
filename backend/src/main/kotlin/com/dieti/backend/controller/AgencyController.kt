package com.dieti.backend.controller

import com.dieti.backend.dto.AgenziaDTO
import com.dieti.backend.dto.AgenteDTO
import com.dieti.backend.repository.AgenziaRepository
import com.dieti.backend.repository.AgenteRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = ["*"])
class AgencyController(
    private val agenziaRepository: AgenziaRepository,
    private val agenteRepository: AgenteRepository
) {

    @GetMapping("/agenzie")
    fun getAllAgenzie(): List<AgenziaDTO> {
        return agenziaRepository.findAll().map {
            AgenziaDTO(nome = it.nome)
        }
    }

    @GetMapping("/agenti")
    fun getAllAgenti(): List<AgenteDTO> {
        return agenteRepository.findAll().map {
            AgenteDTO(
                id = it.uuid.toString(),
                nome = it.nome,
                cognome = it.cognome,
                email = it.email,
                agenziaNome = it.agenzia.nome
            )
        }
    }
}
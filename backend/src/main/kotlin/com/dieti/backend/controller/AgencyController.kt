package com.dieti.backend.controller

import com.dieti.backend.dto.AgenziaDTO
import com.dieti.backend.dto.AgenteDTO
import com.dieti.backend.repository.AgenziaRepository
import com.dieti.backend.repository.AgenteRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = ["*"])
class AgencyController(
    private val agenziaRepository: AgenziaRepository,
    private val agenteRepository: AgenteRepository
) {

    // --- AGENZIE ---
    @GetMapping("/agenzie")
    fun getAllAgenzie(): List<AgenziaDTO> {
        return agenziaRepository.findAll().map {
            AgenziaDTO(it.id ?: 0, it.nome, it.partitaIva, it.indirizzo, it.email)
        }
    }

    // --- AGENTI ---
    @GetMapping("/agenti")
    fun getAllAgenti(): List<AgenteDTO> {
        return agenteRepository.findAll().map {
            AgenteDTO(
                id = it.id ?: 0,
                nome = it.nome,
                cognome = it.cognome,
                email = it.email,
                telefono = it.telefono,
                agenziaNome = it.agenzia?.nome
            )
        }
    }

    // Esempio endpoint per promuovere un utente ad agente (se serve)
    // @PostMapping("/promuovi/{userId}") ...
}
package com.dieti.backend.service

import com.dieti.backend.dto.AgenziaOptionDTO
import com.dieti.backend.dto.CreateAgenziaRequest
import com.dieti.backend.entity.AgenziaEntity
import com.dieti.backend.repository.AgenziaRepository
import com.dieti.backend.repository.AmministratoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AgenziaService(
    private val agenziaRepository: AgenziaRepository,
    private val amministratoreRepository: AmministratoreRepository
) {

    @Transactional
    fun creaAgenzia(request: CreateAgenziaRequest): AgenziaEntity {
        val admin = amministratoreRepository.findById(request.adminId)
            .orElseThrow { IllegalArgumentException("Amministratore non trovato") }

        val agenzia = AgenziaEntity(
            nome = request.nome,
            indirizzo = request.indirizzo,
            amministratore = admin
        )
        return agenziaRepository.save(agenzia)
    }

    /**
     * Restituisce una lista di DTO leggeri per popolare la dropdown nel frontend.
     * Calcola al volo se l'agenzia ha già un capo.
     */
    @Transactional(readOnly = true)
    fun getAgenzieOptionsForAdmin(): List<AgenziaOptionDTO> {
        val agenzie = agenziaRepository.findAll()

        return agenzie.map { agenzia ->
            // Controlla nella lista degli agenti se ce n'è uno con isCapo = true
            val haCapo = agenzia.agenti.any { it.isCapo }

            AgenziaOptionDTO(
                id = agenzia.uuid.toString(),
                nome = agenzia.nome,
                haCapo = haCapo
            )
        }
    }
}
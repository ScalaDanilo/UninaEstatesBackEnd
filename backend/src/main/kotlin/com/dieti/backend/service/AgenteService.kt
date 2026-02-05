package com.dieti.backend.service

import com.dieti.backend.entity.AgenteEntity
import com.dieti.backend.repository.AgenteRepository
import org.hibernate.Hibernate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AgenteService(
    private val agenteRepository: AgenteRepository
) {

    @Transactional(readOnly = true)
    fun getAgenteById(uuid: UUID): AgenteEntity {
        val agente = agenteRepository.findById(uuid).orElseThrow {
            RuntimeException("Agente non trovato con ID: $uuid")
        }

        // FIX LAZY LOADING:
        // L'entità Agente ha @ManyToOne(fetch = FetchType.LAZY) su 'agenzia'.
        // Dobbiamo inizializzarla qui, altrimenti quando il DTO proverà a leggere
        // 'agenzia.nome', la sessione sarà chiusa e crasherà.
        Hibernate.initialize(agente.agenzia)

        return agente
    }
}
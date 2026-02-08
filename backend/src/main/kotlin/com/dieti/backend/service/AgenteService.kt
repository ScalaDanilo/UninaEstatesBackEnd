package com.dieti.backend.service

import com.dieti.backend.dto.CreateAgenteRequest
import com.dieti.backend.entity.AgenteEntity
import com.dieti.backend.repository.AgenteRepository
import com.dieti.backend.repository.AgenziaRepository
import org.hibernate.Hibernate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AgenteService(
    private val agenteRepository: AgenteRepository,
    private val agenziaRepository: AgenziaRepository,
    private val passwordEncoder: PasswordEncoder
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
    
    @Transactional
    fun creaAgente(request: CreateAgenteRequest): AgenteEntity {
        val agenzia = agenziaRepository.findById(request.agenziaId)
            .orElseThrow { IllegalArgumentException("Agenzia non trovata") }

        if (agenteRepository.findByEmail(request.email) != null) {
            throw IllegalArgumentException("Email agente già esistente")
        }

        val agente = AgenteEntity(
            nome = request.nome,
            cognome = request.cognome,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            isCapo = request.isCapo,
            agenzia = agenzia
        )

        return agenteRepository.save(agente)
    }

    fun getAllAgenti(): List<AgenteEntity> = agenteRepository.findAll()
}
package com.dieti.backend.service

import com.dieti.backend.dto.CreateAgenteRequest
import com.dieti.backend.entity.AgenteEntity
import com.dieti.backend.repository.AgenteRepository
import com.dieti.backend.repository.AgenziaRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AgenteService(
    private val agenteRepository: AgenteRepository,
    private val agenziaRepository: AgenziaRepository,
    private val passwordEncoder: PasswordEncoder
) {

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
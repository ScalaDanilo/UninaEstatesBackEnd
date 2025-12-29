package com.dieti.backend.service

import com.dieti.backend.repository.AgenteRepository
import com.dieti.backend.entity.AgenteEntity
import org.springframework.stereotype.Service

@Service
class AgenteService (private val agenteRepository: AgenteRepository) {
    fun prendiTuttiGliAgenti(): List<AgenteEntity> = agenteRepository.findAll()
}
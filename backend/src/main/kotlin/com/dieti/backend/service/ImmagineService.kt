package com.dieti.backend.service

import com.dieti.backend.entity.ImmagineEntity
import com.dieti.backend.repository.ImmagineRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ImmagineService(
    private val immagineRepository: ImmagineRepository
) {

    @Transactional(readOnly = true)
    fun getImmagineContent(id: Int): ImmagineEntity {
        return immagineRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Immagine con ID $id non trovata") }
    }
}
package com.dieti.backend.repository

import com.dieti.backend.entity.OffertaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OffertaRepository : JpaRepository<OffertaEntity, UUID> {
    // Trova tutte le offerte ricevute per un certo immobile
    fun findByImmobileUuid(immobileId: UUID): List<OffertaEntity>
}
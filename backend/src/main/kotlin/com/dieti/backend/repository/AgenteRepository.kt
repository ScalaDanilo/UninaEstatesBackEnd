package com.dieti.backend.repository

import com.dieti.backend.entity.AgenteEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AgenteRepository : JpaRepository<AgenteEntity, UUID> {
    fun findByEmail(email: String): AgenteEntity?
    fun findAllByAgenziaUuid(agenziaId: UUID): List<AgenteEntity>
}
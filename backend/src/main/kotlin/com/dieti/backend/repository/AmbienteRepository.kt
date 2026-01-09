package com.dieti.backend.repository

import com.dieti.backend.entity.AmbienteEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AmbienteRepository : JpaRepository<AmbienteEntity, UUID> {
    fun findByImmobileUuid(uuid: UUID): List<AmbienteEntity>}

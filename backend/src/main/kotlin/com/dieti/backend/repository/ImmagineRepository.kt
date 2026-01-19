package com.dieti.backend.repository

import com.dieti.backend.entity.ImmagineEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ImmagineRepository : JpaRepository<ImmagineEntity, Int> {
    fun findByImmobileUuid(uuid: UUID): List<ImmagineEntity>
}
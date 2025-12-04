package com.dieti.backend.repository

import com.dieti.backend.entity.AmministratoreEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AmministratoreRepository : JpaRepository<AmministratoreEntity, UUID> {
    fun findByEmail(email: String): AmministratoreEntity?
}
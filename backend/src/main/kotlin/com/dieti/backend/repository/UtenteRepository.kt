package com.dieti.backend.repository

import com.dieti.backend.entity.UtenteRegistratoEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UtenteRepository : JpaRepository<UtenteRegistratoEntity, UUID> {
    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): UtenteRegistratoEntity?
}
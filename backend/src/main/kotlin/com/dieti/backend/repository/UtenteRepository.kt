package com.dieti.backend.repository

import com.dieti.backend.entity.UtenteRegistratoEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UtenteRepository : JpaRepository<UtenteRegistratoEntity, UUID> {
    // Spring crea automaticamente la query SQL basandosi sul nome della funzione
    fun existsByEmail(email: String): Boolean

    // Ci servirà per il login
    fun findByEmail(email: String): UtenteRegistratoEntity?
}
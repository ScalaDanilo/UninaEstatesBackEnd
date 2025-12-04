package com.dieti.backend.repository

import com.dieti.backend.entity.UtenteRegistratoEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UtenteRepository : JpaRepository<UtenteRegistratoEntity, UUID> {
    // Fondamentale per il Login: cerca un utente data la sua email
    fun findByEmail(email: String): UtenteRegistratoEntity?
}
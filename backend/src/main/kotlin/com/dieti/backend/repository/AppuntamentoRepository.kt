package com.dieti.backend.repository

import com.dieti.backend.entity.AppuntamentoEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AppuntamentoRepository : JpaRepository<AppuntamentoEntity, UUID> {
    // Utile per mostrare a un utente i suoi appuntamenti
    fun findByUtenteUuid(userId: UUID): List<AppuntamentoEntity>
}
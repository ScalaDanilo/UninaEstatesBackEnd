package com.dieti.backend.repository

import com.dieti.backend.entity.UltimaRicercaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UltimaRicercaRepository : JpaRepository<UltimaRicercaEntity, UUID> {
    // Trova le ricerche di un utente (magari limitando a 5 dal service)
    fun findByUtenteNonRegistratoUuid(userId: UUID): List<UltimaRicercaEntity>
}
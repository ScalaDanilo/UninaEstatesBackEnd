package com.dieti.backend.repository

import com.dieti.backend.entity.NotificaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificaRepository : JpaRepository<NotificaEntity, UUID> {
    // Trova tutte le notifiche di un utente ordinate per data (più recenti prima)
    fun findByUtenteUuidOrderByDataCreazioneDesc(userId: UUID): List<NotificaEntity>
    
    // Conta quante notifiche non lette ha un utente
    fun countByUtenteUuidAndLettoFalse(userId: UUID): Long
}
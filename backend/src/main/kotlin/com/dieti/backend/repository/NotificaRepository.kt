package com.dieti.backend.repository

import com.dieti.backend.entity.NotificaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificaRepository : JpaRepository<NotificaEntity, UUID> {
    fun findByUtenteUuidOrderByDataCreazioneDesc(userId: UUID): List<NotificaEntity>

    fun countByUtenteUuidAndLettoFalse(userId: UUID): Long
}
package com.dieti.backend.repository

import com.dieti.backend.entity.UltimaRicercaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UltimaRicercaRepository : JpaRepository<UltimaRicercaEntity, UUID> {

    // Trova le ricerche ordinandole per data (dalla più recente)
    fun findAllByUtenteRegistratoEmailOrderByDataDesc(email: String): List<UltimaRicercaEntity>

    // Trova una specifica ricerca per cancellarla
    fun findByUtenteRegistratoEmailAndCorpoIgnoreCase(email: String, corpo: String): UltimaRicercaEntity?
}
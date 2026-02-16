package com.dieti.backend.repository

import com.dieti.backend.entity.UltimaRicercaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UltimaRicercaRepository : JpaRepository<UltimaRicercaEntity, UUID> {

    fun findAllByUtenteRegistratoEmailOrderByDataDesc(email: String): List<UltimaRicercaEntity>

    fun findByUtenteRegistratoEmailAndCorpoIgnoreCase(email: String, corpo: String): UltimaRicercaEntity?
}
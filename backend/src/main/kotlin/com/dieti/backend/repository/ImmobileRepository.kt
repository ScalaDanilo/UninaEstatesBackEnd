package com.dieti.backend.repository

import com.dieti.backend.entity.ImmobileEntity
import com.dieti.backend.entity.UtenteRegistratoEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ImmobileRepository : JpaRepository<ImmobileEntity, UUID> {
    // Qui in futuro potrai aggiungere filtri, es:
    fun findAllByProprietarioUuid(uuid: UUID): List<ImmobileEntity>

    // Metodo magico di Spring Data: cancella tutti gli immobili dove proprietario == quello passato
    fun deleteByProprietario(proprietario: UtenteRegistratoEntity)
}
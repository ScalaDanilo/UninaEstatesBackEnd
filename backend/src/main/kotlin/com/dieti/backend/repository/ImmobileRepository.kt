package com.dieti.backend.repository

import com.dieti.backend.entity.ImmobileEntity
import com.dieti.backend.entity.UtenteRegistratoEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ImmobileRepository : JpaRepository<ImmobileEntity, UUID>, JpaSpecificationExecutor<ImmobileEntity> {

    fun findAllByProprietarioUuid(proprietarioUuid: UUID): List<ImmobileEntity>

    fun deleteByProprietario(proprietario: UtenteRegistratoEntity)

    @Query("SELECT i FROM ImmobileEntity i WHERE i.uuid = :uuid AND i.proprietario.email = :email")
    fun findByUuidAndOwnerEmail(@Param("uuid") uuid: UUID, @Param("email") email: String): ImmobileEntity?

    @Query("SELECT DISTINCT i.localita FROM ImmobileEntity i WHERE i.localita IS NOT NULL AND i.localita <> '' AND i.localita <> 'Non specificato'")
    fun findDistinctLocalita(): List<String>

    // --- FIX QUERY MANAGER ---
    // Logica Esplicita: Prendi immobili dell'agenzia specificata CHE NON HANNO un agente (agente IS NULL)
    @Query("SELECT i FROM ImmobileEntity i WHERE i.agenzia.uuid = :agenziaId AND i.agente IS NULL")
    fun findRichiestePendentiPerAgenzia(@Param("agenziaId") agenziaId: UUID): List<ImmobileEntity>
}
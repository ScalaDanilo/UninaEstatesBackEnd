package com.dieti.backend.repository

import com.dieti.backend.entity.OffertaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OffertaRepository : JpaRepository<OffertaEntity, UUID> {

    fun findAllByVenditoreUuidOrderByDataOffertaDesc(venditoreId: UUID): List<OffertaEntity>
    fun existsByOfferenteUuidAndImmobileUuid(offerenteId: UUID, immobileId: UUID): Boolean
    fun findAllByOfferenteUuid(offerenteId: UUID): List<OffertaEntity>

    // Query vecchia (solo nuove): Mantienila se serve altrove, ma useremo quella sotto
    @Query("SELECT o FROM OffertaEntity o WHERE o.immobile.agente.uuid = :agenteId AND NOT EXISTS (SELECT 1 FROM RispostaEntity r WHERE r.offerta.uuid = o.uuid) ORDER BY o.dataOfferta DESC")
    fun findOffertePendenti(@Param("agenteId") agenteId: UUID): List<OffertaEntity>

    // NUOVA QUERY: Trova TUTTE le trattative gestite dall'agente
    @Query("SELECT o FROM OffertaEntity o WHERE o.immobile.agente.uuid = :agenteId ORDER BY o.dataOfferta DESC")
    fun findAllByAgente(@Param("agenteId") agenteId: UUID): List<OffertaEntity>
}
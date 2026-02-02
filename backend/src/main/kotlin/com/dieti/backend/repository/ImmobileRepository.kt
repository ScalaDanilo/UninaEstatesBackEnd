package com.dieti.backend.repository

import com.dieti.backend.entity.ImmobileEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ImmobileRepository : JpaRepository<ImmobileEntity, UUID>, JpaSpecificationExecutor<ImmobileEntity> {

    fun findAllByProprietarioUuid(uuid: UUID): List<ImmobileEntity>

    // FIX: JPQL richiede il nome della classe (ImmobileEntity), non della tabella (immobile)
    @Query("SELECT DISTINCT i.localita FROM ImmobileEntity i WHERE i.localita IS NOT NULL AND i.localita <> '' AND i.localita <> 'Non specificato'")
    fun findDistinctLocalita(): List<String>
}
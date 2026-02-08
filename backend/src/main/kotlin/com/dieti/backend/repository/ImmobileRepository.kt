package com.dieti.backend.repository

import com.dieti.backend.entity.ImmobileEntity
import com.dieti.backend.entity.UtenteRegistratoEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ImmobileRepository : JpaRepository<ImmobileEntity, UUID>, JpaSpecificationExecutor<ImmobileEntity> {

    fun findAllByProprietarioUuid(proprietarioUuid: UUID): List<ImmobileEntity>

    // Metodo magico di Spring Data: cancella tutti gli immobili dove proprietario == quello passato
    fun deleteByProprietario(proprietario: UtenteRegistratoEntity)


    // NUOVO: Trova immobile per UUID e Email Proprietario (per sicurezza in fase di edit/delete)
    @Query("SELECT i FROM ImmobileEntity i WHERE i.uuid = :uuid AND i.proprietario.email = :email")
    fun findByUuidAndOwnerEmail(uuid: UUID, email: String): ImmobileEntity?
    
    // FIX: JPQL richiede il nome della classe (ImmobileEntity), non della tabella (immobile)
    @Query("SELECT DISTINCT i.localita FROM ImmobileEntity i WHERE i.localita IS NOT NULL AND i.localita <> '' AND i.localita <> 'Non specificato'")
    fun findDistinctLocalita(): List<String>
}
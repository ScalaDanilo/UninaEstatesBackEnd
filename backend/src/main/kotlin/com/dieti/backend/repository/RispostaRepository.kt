package com.dieti.backend.repository

import com.dieti.backend.entity.RispostaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RispostaRepository : JpaRepository<RispostaEntity, Int> {

    fun findAllByOffertaUuidOrderByDataRispostaAsc(offertaId: UUID): List<RispostaEntity>
}
package com.dieti.backend.repository

import com.dieti.backend.entity.AgenziaEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface AgenziaRepository : JpaRepository<AgenziaEntity, UUID> {

    // Calcola la distanza Euclidea quadrata (più veloce della radice) e ordina i risultati
    @Query("SELECT a FROM AgenziaEntity a ORDER BY (POWER(a.lat - :lat, 2) + POWER(a.long - :lon, 2)) ASC")
    fun findNearestAgencyList(@Param("lat") lat: Double, @Param("lon") lon: Double, pageable: Pageable): List<AgenziaEntity>
}
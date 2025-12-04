package com.dieti.backend.repository

import com.dieti.backend.entity.ImmobileEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ImmobileRepository : JpaRepository<ImmobileEntity, UUID> {
    // Qui in futuro potrai aggiungere filtri, es:
    // fun findByPrezzoLessThan(maxPrezzo: Int): List<ImmobileEntity>
}
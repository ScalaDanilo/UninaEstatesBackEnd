package com.dieti.backend.repository

import com.dieti.backend.entity.RispostaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RispostaRepository : JpaRepository<RispostaEntity, Int>
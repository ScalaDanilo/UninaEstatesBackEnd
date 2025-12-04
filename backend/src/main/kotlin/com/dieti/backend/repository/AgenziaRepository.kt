package com.dieti.backend.repository

import com.dieti.backend.entity.AgenziaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AgenziaRepository : JpaRepository<AgenziaEntity, String>
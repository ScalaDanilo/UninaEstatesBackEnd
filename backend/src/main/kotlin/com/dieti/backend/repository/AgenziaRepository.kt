package com.dieti.backend.repository

import com.dieti.backend.entity.AgenziaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

// Nota: ID è UUID, non String
interface AgenziaRepository : JpaRepository<AgenziaEntity, UUID>
package com.dieti.backend.repository
import com.dieti.backend.entity.AgenteEntity // Cambia entità in base al file
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AgenteRepository : JpaRepository<AgenteEntity, UUID>
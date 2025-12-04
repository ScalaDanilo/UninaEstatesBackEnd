package com.dieti.backend.repository

import com.dieti.backend.entity.SegnalazioneEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SegnalazioneRepository : JpaRepository<SegnalazioneEntity, UUID>
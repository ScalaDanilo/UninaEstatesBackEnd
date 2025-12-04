package com.dieti.backend.repository

import com.dieti.backend.entity.ImmagineEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ImmagineRepository : JpaRepository<ImmagineEntity, Int>
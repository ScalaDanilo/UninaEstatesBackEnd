package com.dieti.backend.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "amministratore")
data class AmministratoreEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    @Column(unique = true)
    val email: String,
    val password: String
)
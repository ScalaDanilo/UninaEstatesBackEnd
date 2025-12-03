package com.dieti.backend.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "utente_non_registrato")
data class UtenteNonRegistratoEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null
)
package com.dieti.backend.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "ultima_ricerca")
data class UltimaRicercaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    val corpo: String? = null,
    val data: LocalDate = LocalDate.now(),

    @ManyToOne
    @JoinColumn(name = "user_id")
    val utenteNonRegistrato: UtenteNonRegistratoEntity
)
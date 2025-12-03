package com.dieti.backend.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(name = "appuntamento")
data class AppuntamentoEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val utente: UtenteRegistratoEntity,

    @ManyToOne
    @JoinColumn(name = "agente_id")
    val agente: AgenteEntity,

    @ManyToOne
    @JoinColumn(name = "immobile_id")
    val immobile: ImmobileEntity,

    val data: LocalDate,
    val ora: LocalTime,
    val corpo: String? = null
)
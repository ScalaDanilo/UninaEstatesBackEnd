package com.dieti.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "ambienti")
data class AmbienteEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "immobile_id")
    var immobile: ImmobileEntity? = null,

    val tipologia: String? = null,
    val numero: Int? = null
)
package com.dieti.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "immagini")
data class ImmagineEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "immobile_id")
    var immobile: ImmobileEntity? = null,

    val nome: String? = null,
    val formato: String? = null,

    @Column(name = "immagine", columnDefinition = "bytea")
    val immagine: ByteArray? = null
)
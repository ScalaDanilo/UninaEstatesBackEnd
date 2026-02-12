package com.dieti.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "offerta")
data class OffertaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    // Allineato con la foto: user_offerente_id
    @ManyToOne
    @JoinColumn(name = "user_offerente_id")
    val offerente: UtenteRegistratoEntity,

    // Allineato con la foto: user_venditore_id
    @ManyToOne
    @JoinColumn(name = "user_venditore_id")
    val venditore: UtenteRegistratoEntity,

    @ManyToOne
    @JoinColumn(name = "immobile_id")
    val immobile: ImmobileEntity,

    @Column(name = "prezzo_offerta")
    val prezzoOfferta: Int,

    val corpo: String? = null,

    @Column(name = "data_offerta")
    val dataOfferta: LocalDateTime = LocalDateTime.now()
)
package com.dieti.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "risposta")
data class RispostaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offerta_id")
    val offerta: OffertaEntity,

    @ManyToOne
    @JoinColumn(name = "venditore_id")
    val mittente: UtenteRegistratoEntity,

    @ManyToOne
    @JoinColumn(name = "compratore_id")
    val destinatario: UtenteRegistratoEntity,

    @Column(name = "prezzo_offerta")
    val prezzoProposto: Int? = null,

    val corpo: String? = null,
    val tipo: String,

    @Column(name = "data_risposta")
    val dataRisposta: LocalDateTime = LocalDateTime.now()
)
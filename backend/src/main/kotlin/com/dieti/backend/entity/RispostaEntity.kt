package com.dieti.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "risposta")
data class RispostaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @OneToOne
    @JoinColumn(name = "offerta_id")
    val offerta: OffertaEntity,

    @ManyToOne
    @JoinColumn(name = "venditore_id")
    val venditore: UtenteRegistratoEntity,

    @ManyToOne
    @JoinColumn(name = "compratore_id")
    val compratore: UtenteRegistratoEntity,

    @Column(name = "prezzo_offerta")
    val prezzoOfferta: Int? = null,
    val corpo: String? = null,
    val tipo: String? = null, // Accettata/Rifiutata
    
    @Column(name = "data_risposta")
    val dataRisposta: LocalDateTime = LocalDateTime.now()
)
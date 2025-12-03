package com.dieti.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "segnalazioni")
data class SegnalazioneEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val utente: UtenteRegistratoEntity,

    @ManyToOne
    @JoinColumn(name = "immobile_id")
    val immobile: ImmobileEntity,

    @ManyToOne
    @JoinColumn(name = "amministratore_id")
    val amministratore: AmministratoreEntity? = null,

    val titolo: String? = null,
    val corpo: String? = null,
    
    @Column(name = "data_segnalazione")
    val dataSegnalazione: LocalDateTime = LocalDateTime.now()
)
package com.dieti.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "notifica")
data class NotificaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val utente: UtenteRegistratoEntity,

    val titolo: String,
    val corpo: String? = null,
    val letto: Boolean = false,
    val tipo: String = "GENERALE",
    
    @Column(name = "data_creazione")
    val dataCreazione: LocalDateTime = LocalDateTime.now()
)
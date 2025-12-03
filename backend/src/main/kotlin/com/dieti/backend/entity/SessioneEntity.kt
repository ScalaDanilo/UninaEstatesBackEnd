package com.dieti.backend.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "sessione")
data class SessioneEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "token_id")
    val tokenId: UUID? = null,

    val stato: Boolean = true,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val utenteNonRegistrato: UtenteNonRegistratoEntity
)
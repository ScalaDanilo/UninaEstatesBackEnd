package com.dieti.backend.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "agente")
data class AgenteEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    val nome: String,
    val cognome: String,
    @Column(unique = true)
    val email: String,
    val password: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nome_agenzia")
    val agenzia: AgenziaEntity
)
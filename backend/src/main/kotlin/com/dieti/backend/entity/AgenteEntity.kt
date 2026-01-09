package com.dieti.backend.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "agente")
class AgenteEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    val nome: String,
    val cognome: String,
    @Column(unique = true)
    val email: String,
    val password: String,

    // --- CORREZIONE QUI ---
    // Il nome della variabile DEVE essere "agenzia" perché nell'altra classe
    // hai usato mappedBy = "agenzia"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agenzia_id")
    var agenzia: AgenziaEntity? = null
)
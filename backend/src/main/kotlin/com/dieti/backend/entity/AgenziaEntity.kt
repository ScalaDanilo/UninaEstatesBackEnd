package com.dieti.backend.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "agenzia")
class AgenziaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val nome: String,


    // --- CORREZIONE QUI ---
    @OneToMany(mappedBy = "agenzia", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var agenti: MutableList<AgenteEntity> = mutableListOf()
)
package com.dieti.backend.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "agente")
class AgenteEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var uuid: UUID? = null,

    var nome: String,
    var cognome: String,

    @Column(unique = true, nullable = false)
    var email: String,

    var password: String,

    // Flag booleano per il ruolo (Capo Agenzia vs Agente Semplice)
    @Column(name = "is_capo")
    var isCapo: Boolean = false,

    // Relazione: Ogni agente appartiene a 1 agenzia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agenzia_id", nullable = false)
    @JsonIgnore
    var agenzia: AgenziaEntity
)
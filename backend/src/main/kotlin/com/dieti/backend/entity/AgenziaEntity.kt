package com.dieti.backend.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "agenzia")
class AgenziaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var uuid: UUID? = null,

    var nome: String,
    var indirizzo: String? = null,

    // Relazione: Ogni agenzia ha 1 amministratore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amministratore_id", nullable = false)
    @JsonIgnore // Evita loop infiniti nel JSON
    var amministratore: AmministratoreEntity,

    // Relazione: Un'agenzia ha N agenti
    @OneToMany(mappedBy = "agenzia", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var agenti: MutableList<AgenteEntity> = mutableListOf()
)
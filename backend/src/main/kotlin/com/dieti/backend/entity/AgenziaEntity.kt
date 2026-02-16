package com.dieti.backend.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "agenzia")
class AgenziaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var uuid: UUID? = null,

    var nome: String,
    var indirizzo: String,

    var lat: Double = 0.0,
    var long: Double = 0.0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amministratore_id")
    var amministratore: AmministratoreEntity? = null
) {
    @OneToMany(mappedBy = "agenzia", fetch = FetchType.LAZY)
    var agenti: MutableList<AgenteEntity> = mutableListOf()

    @OneToMany(mappedBy = "agenzia", fetch = FetchType.LAZY)
    var immobili: MutableList<ImmobileEntity> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AgenziaEntity) return false
        return uuid != null && uuid == other.uuid
    }

    override fun hashCode(): Int = 31
}
package com.dieti.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "ambienti")
class AmbienteEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "immobile_id")
    var immobile: ImmobileEntity? = null,

    var tipologia: String,
    var numero: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AmbienteEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = 31

    override fun toString(): String {
        return "AmbienteEntity(id=$id, tipologia='$tipologia')"
    }
}
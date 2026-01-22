package com.dieti.backend.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "utente_registrato")
class UtenteRegistratoEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var uuid: UUID? = null,

    var nome: String,
    var cognome: String,

    @Column(unique = true)
    var email: String,

    var password: String? = null,
    var telefono: String? = null
) {
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "preferiti",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "immobile_id")]
    )
    var preferiti: MutableList<ImmobileEntity> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UtenteRegistratoEntity) return false
        return uuid != null && uuid == other.uuid
    }

    override fun hashCode(): Int = 31

    override fun toString(): String {
        return "UtenteRegistratoEntity(uuid=$uuid, email='$email')"
    }
}
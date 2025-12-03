package com.dieti.backend.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "utente_registrato")
data class UtenteRegistratoEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    val nome: String,
    val cognome: String,
    @Column(unique = true)
    val email: String,
    val password: String? = null,
    val telefono: String? = null,

    // Relazione Preferiti (Molti a Molti)
    @ManyToMany
    @JoinTable(
        name = "preferiti",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "immobile_id")]
    )
    val preferiti: MutableList<ImmobileEntity> = mutableListOf()
)
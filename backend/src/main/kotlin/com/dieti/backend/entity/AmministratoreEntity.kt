package com.dieti.backend.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "amministratore")
class AmministratoreEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var uuid: UUID? = null,

    @Column(unique = true, nullable = false)
    var email: String,

    var password: String,

    // Relazione: Un amministratore possiede N agenzie
    @OneToMany(mappedBy = "amministratore", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var agenzie: MutableList<AgenziaEntity> = mutableListOf()
)
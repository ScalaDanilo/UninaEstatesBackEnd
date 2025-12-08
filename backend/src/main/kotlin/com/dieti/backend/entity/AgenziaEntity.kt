package com.dieti.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "agenzia")
data class AgenziaEntity(
    @Id
    @Column(name = "nome", length = 255)
    val nome: String, // PK come da SQL

    // Relazione inversa per accedere agli agenti dell'agenzia
    @OneToMany(mappedBy = "agenzia", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val agenti: MutableList<AgenteEntity> = mutableListOf()
)
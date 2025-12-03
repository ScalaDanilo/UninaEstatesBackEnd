package com.dieti.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "agenzia")
data class AgenziaEntity(
    @Id
    @Column(name = "nome")
    val nome: String, // PK è una stringa

    @OneToMany(mappedBy = "agenzia", cascade = [CascadeType.ALL])
    val agenti: MutableList<AgenteEntity> = mutableListOf()
)
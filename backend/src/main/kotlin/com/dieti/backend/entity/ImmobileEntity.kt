package com.dieti.backend.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "immobile")
data class ImmobileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val uuid: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val proprietario: UtenteRegistratoEntity,

    @Column(name = "tipo_vendita")
    val tipoVendita: Boolean, // true = Vendita
    
    val categoria: String? = null,
    val tipologia: String? = null,
    val localita: String? = null,
    val mq: Int? = null,
    val piano: Int? = null,
    val ascensore: Boolean? = null,
    val dettagli: String? = null, // TEXT in SQL è String in Kotlin
    val arredamento: String? = null,
    val climatizzazione: Boolean? = null,
    val esposizione: String? = null,
    @Column(name = "tipo_proprieta")
    val tipoProprieta: String? = null,
    @Column(name = "stato_proprieta")
    val statoProprieta: String? = null,
    @Column(name = "anno_costruzione")
    val annoCostruzione: LocalDate? = null,
    val prezzo: Int? = null,
    @Column(name = "spese_condominiali")
    val speseCondominiali: Int? = null,
    val disponibilita: Boolean = true,
    val descrizione: String? = null,

    // Relazioni Figlie
    @OneToMany(mappedBy = "immobile", cascade = [CascadeType.ALL], orphanRemoval = true)
    val immagini: MutableList<ImmagineEntity> = mutableListOf(),

    @OneToMany(mappedBy = "immobile", cascade = [CascadeType.ALL], orphanRemoval = true)
    val ambienti: MutableList<AmbienteEntity> = mutableListOf()
)
package com.dieti.backend.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "immobile")
class ImmobileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var uuid: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var proprietario: UtenteRegistratoEntity,

    @Column(name = "tipo_vendita")
    var tipoVendita: Boolean = false,

    var categoria: String? = null,
    var indirizzo: String? = null,
    var localita: String? = null,
    var mq: Int? = null,
    var piano: Int? = null,
    var ascensore: Boolean? = null,
    var arredamento: String? = null,
    var climatizzazione: Boolean? = null,
    var esposizione: String? = null,

    @Column(name = "tipo_proprieta")
    var tipoProprieta: String? = null,

    @Column(name = "stato_proprieta")
    var statoProprieta: String? = null,

    @Column(name = "anno_costruzione")
    var annoCostruzione: LocalDate? = null,

    var prezzo: Int? = null,

    @Column(name = "spese_condominiali")
    var speseCondominiali: Int? = null,

    var descrizione: String? = null
) {
    // Relazioni definite fuori dal costruttore per evitare problemi con toString() automatici
    @OneToMany(mappedBy = "immobile", cascade = [CascadeType.ALL], orphanRemoval = true)
    var immagini: MutableList<ImmagineEntity> = mutableListOf()

    @OneToMany(mappedBy = "immobile", cascade = [CascadeType.ALL], orphanRemoval = true)
    var ambienti: MutableList<AmbienteEntity> = mutableListOf()

    // --- METODI SICURI PER HIBERNATE ---

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ImmobileEntity) return false
        return uuid != null && uuid == other.uuid
    }

    override fun hashCode(): Int {
        // Restituisce un codice costante per evitare problemi quando l'ID cambia dopo il salvataggio
        return 31
    }

    override fun toString(): String {
        return "ImmobileEntity(uuid=$uuid, indirizzo=$indirizzo)"
    }
}
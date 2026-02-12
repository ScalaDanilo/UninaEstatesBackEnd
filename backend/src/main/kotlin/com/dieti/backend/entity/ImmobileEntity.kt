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

    // L'Agenzia di competenza territoriale (Assegnata automaticamente via Geo al caricamento)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agenzia_id")
    var agenzia: AgenziaEntity? = null,

    // È nullable (?) perché all'inizio l'immobile è "in attesa" di accettazione
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id")
    var agente: AgenteEntity? = null,

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

    var descrizione: String? = null,

    // --- NUOVI PARAMETRI GEOGRAFICI & SERVIZI ---
    var lat: Double? = null,
    var long: Double? = null,

    var parco: Boolean = false,
    var scuola: Boolean = false,
    var servizioPubblico: Boolean = false

) {
    @OneToMany(mappedBy = "immobile", cascade = [CascadeType.ALL], orphanRemoval = true)
    var immagini: MutableList<ImmagineEntity> = mutableListOf()

    @OneToMany(mappedBy = "immobile", cascade = [CascadeType.ALL], orphanRemoval = true)
    var ambienti: MutableList<AmbienteEntity> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ImmobileEntity) return false
        return uuid != null && uuid == other.uuid
    }

    override fun hashCode(): Int = 31

    override fun toString(): String {
        return "ImmobileEntity(uuid=$uuid, indirizzo=$indirizzo)"
    }
}
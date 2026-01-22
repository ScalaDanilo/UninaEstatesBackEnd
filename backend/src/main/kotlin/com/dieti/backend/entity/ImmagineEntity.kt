package com.dieti.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "immagini")
class ImmagineEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "immobile_id")
    var immobile: ImmobileEntity? = null,

    var nome: String? = null,
    var formato: String? = null,

    // FIX: Rimosso @Lob.
    // @Lob in Postgres mappa su OID (numero intero/BigInt), causando l'errore di tipo.
    // Usando columnDefinition = "bytea", forziamo il salvataggio dei byte grezzi.
    @Column(name = "immagine", columnDefinition = "bytea")
    var immagine: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ImmagineEntity) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = 31

    override fun toString(): String {
        return "ImmagineEntity(id=$id, nome=$nome)"
    }
}
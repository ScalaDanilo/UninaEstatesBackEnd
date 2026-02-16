package com.dieti.backend.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "ultima_ricerca")
class UltimaRicercaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var uuid: UUID? = null,

    var corpo: String? = null,

    var data: LocalDate = LocalDate.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_registrato_id")
    var utenteRegistrato: UtenteRegistratoEntity? = null

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UltimaRicercaEntity) return false
        return uuid != null && uuid == other.uuid
    }

    override fun hashCode(): Int = 31
}
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
    // *** FIX CRITICO ***
    // Prima puntava a "user_id", ma quella colonna nel tuo DB accetta solo utenti NON registrati.
    // Cambiando il nome in "utente_registrato_id", Hibernate creerà una NUOVA colonna pulita
    // dedicata agli utenti registrati, aggirando l'errore di Foreign Key.
    @JoinColumn(name = "utente_registrato_id")
    var utenteRegistrato: UtenteRegistratoEntity? = null

    // Rimosso il campo utenteNonRegistrato per evitare confusione.
    // La vecchia colonna "user_id" rimarrà nel DB ma verrà ignorata da questo codice.
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UltimaRicercaEntity) return false
        return uuid != null && uuid == other.uuid
    }

    override fun hashCode(): Int = 31
}
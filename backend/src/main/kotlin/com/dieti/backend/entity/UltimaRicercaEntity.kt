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
    // FIX: Cambiato da "user_registrato_id" a "utente_registrato_id" per coerenza standard JPA/DB
    @JoinColumn(name = "user_id")
    var utenteRegistrato: UtenteRegistratoEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_non_registrato_id")
    var utenteNonRegistrato: UtenteNonRegistratoEntity? = null
)
package com.dieti.backend.dto

import java.time.LocalDate
import java.util.UUID

// --- IMMOBILI ---

// DTO per inviare i dettagli di un immobile al frontend
data class ImmobileDTO(
    val id: UUID,
    val titolo: String, // Generato es: "Appartamento a Napoli"
    val prezzo: Int,
    val tipologia: String?,
    val localita: String?,
    val mq: Int?,
    val descrizione: String?,
    val coverImageId: Int?, // ID della prima immagine per la miniatura
    val isVendita: Boolean,
    val proprietarioId: UUID
)

// DTO per i dettagli completi (quando clicchi su un immobile)
data class ImmobileDetailDTO(
    val id: UUID,
    val proprietarioNome: String,
    val proprietarioId: UUID,
    val tipoVendita: Boolean,
    val categoria: String?,
    val tipologia: String?,
    val localita: String?,
    val mq: Int?,
    val piano: Int?,
    val ascensore: Boolean?,
    val dettagli: String?,
    val arredamento: String?,
    val climatizzazione: Boolean?,
    val esposizione: String?,
    val tipoProprieta: String?,
    val statoProprieta: String?,
    val annoCostruzione: LocalDate?,
    val prezzo: Int?,
    val speseCondominiali: Int?,
    val disponibilita: Boolean,
    val descrizione: String?,
    val immaginiIds: List<Int> // Lista degli ID delle immagini per scaricarle separatamente
)

// DTO per creare/modificare un immobile (Input dal Frontend)
data class ImmobileCreateRequest(
    val proprietarioId: UUID,
    val tipoVendita: Boolean,
    val categoria: String,
    val tipologia: String,
    val localita: String,
    val mq: Int,
    val piano: Int,
    val ascensore: Boolean,
    val dettagli: String,
    val arredamento: String,
    val climatizzazione: Boolean,
    val esposizione: String,
    val tipoProprieta: String,
    val statoProprieta: String,
    val prezzo: Int,
    val speseCondominiali: Int,
    val descrizione: String
)

// --- UTENTI ---

data class UserProfileDTO(
    val id: UUID,
    val nome: String,
    val cognome: String,
    val email: String,
    val telefono: String?,
    val bio: String? = null // Se vuoi aggiungere una bio
)

data class UserUpdateRequest(
    val telefono: String?,
    val password: String? // Opzionale, solo se vuole cambiarla
)

// --- APPUNTAMENTI ---

data class AppuntamentoRequest(
    val utenteId: UUID,
    val immobileId: UUID,
    val data: LocalDate,
    val orario: String // Es: "10:30"
)

data class AppuntamentoDTO(
    val id: Int,
    val immobileTitolo: String,
    val data: LocalDate,
    val orario: String,
    val stato: String // "IN_ATTESA", "CONFERMATO", "RIFIUTATO"
)

// --- OFFERTE ---

data class OffertaRequest(
    val utenteId: UUID,
    val immobileId: UUID,
    val importo: Int
)

data class OffertaDTO(
    val id: Int,
    val immobileTitolo: String,
    val importo: Int,
    val stato: String // "PENDING", "ACCETTATA", "RIFIUTATA"
)

// --- NOTIFICHE ---

data class NotificaDTO(
    val id: Int,
    val messaggio: String,
    val data: LocalDate,
    val letta: Boolean
)

// --- AUTH ---

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val nome: String,
    val cognome: String,
    val email: String,
    val password: String,
    val telefono: String?
)

data class AuthResponse(
    val token: String, // O sessionId
    val userId: UUID,
    val nome: String,
    val email: String,
    val ruolo: String // "UTENTE", "AGENTE", "ADMIN"
)

// --- AGENZIE E AGENTI ---

data class AgenziaDTO(
    val id: Int,
    val nome: String,
    val partitaIva: String?,
    val indirizzo: String?,
    val email: String?
)

data class AgenteDTO(
    val id: Int,
    val nome: String,
    val cognome: String,
    val email: String?,
    val telefono: String?,
    val agenziaNome: String?
)
package com.dieti.backend.dto

import java.time.LocalDate
import java.util.UUID

// --- IMMOBILI ---
data class ImmobileDTO(
    val id: UUID,
    val titolo: String,
    val prezzo: Int,
    val tipologia: String?,
    val localita: String?,
    val mq: Int?,
    val descrizione: String?,
    val coverImageId: Int?,
    val isVendita: Boolean,
    val proprietarioId: UUID
)

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
    val immaginiIds: List<Int>
)

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
    val bio: String? = null
)

data class UserUpdateRequest(
    val telefono: String?,
    val password: String?
)

// --- APPUNTAMENTI ---
data class AppuntamentoRequest(
    val utenteId: UUID,
    val immobileId: UUID,
    val agenteId: UUID, // Obbligatorio nel DB
    val data: LocalDate,
    val orario: String // HH:mm
)

data class AppuntamentoDTO(
    val id: UUID,
    val immobileTitolo: String,
    val data: LocalDate,
    val orario: String
    // 'stato' rimosso perché non esiste nel DB
)

// --- OFFERTE ---
data class OffertaRequest(
    val utenteId: UUID,
    val immobileId: UUID,
    val importo: Int,
    val corpo: String? = null
)

data class OffertaDTO(
    val id: UUID,
    val immobileTitolo: String,
    val importo: Int,
    val data: String
)

// --- NOTIFICHE ---
data class NotificaDTO(
    val id: UUID,
    val titolo: String,
    val corpo: String?,
    val data: String,
    val letto: Boolean
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

data class GoogleLoginRequest(
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val idToken: String? = null
)

data class AuthResponse(
    val token: String,
    val userId: UUID,
    val nome: String,
    val email: String,
    val ruolo: String
)

// --- AGENZIE E AGENTI ---
data class AgenziaDTO(
    val nome: String
    // Rimossi partitaIva, indirizzo, email per allineamento DB
)

data class AgenteDTO(
    val id: String, // UUID convertito
    val nome: String,
    val cognome: String,
    val email: String,
    val agenziaNome: String
    // Rimosso telefono per allineamento DB
)
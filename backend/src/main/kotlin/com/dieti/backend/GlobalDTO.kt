package com.dieti.backend.dto

import com.dieti.backend.entity.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate

// DTO per la risposta (Invio dati all'app)
data class ImmobileDTO(
    val id: String,
    val tipoVendita: Boolean,
    val categoria: String?,
    val indirizzo: String?,
    val prezzo: Int?,
    val mq: Int?,
    val descrizione: String?,
    // FIX: Cambiato da LocalDate? a String? per compatibilità col Service
    val annoCostruzione: String?,
    val immagini: List<ImmagineDto> = emptyList(),
    val ambienti: List<AmbienteDto> = emptyList()
)

data class ImmagineDto(
    val id: Int,
    val url: String
)

data class AmbienteDto(
    val tipologia: String,
    val numero: Int
)

// DTO per la richiesta (Ricezione dati dall'app)
data class ImmobileCreateRequest(
    val tipoVendita: Boolean,
    val categoria: String?,
    val indirizzo: String?,
    val mq: Int?,
    val piano: Int?,
    val ascensore: Boolean?,
    val arredamento: String?,
    val climatizzazione: Boolean?,
    val esposizione: String?,
    val statoProprieta: String?,
    // FIX: Cambiato da LocalDate? a String? per permettere il parsing manuale
    val annoCostruzione: String?,
    val prezzo: Int?,
    val speseCondominiali: Int?,
    val descrizione: String?,
    val ambienti: List<AmbienteDto> = emptyList()
)

// --- MAPPERS IMMOBILE ---

fun ImmobileCreateRequest.toEntity(proprietario: UtenteRegistratoEntity): ImmobileEntity {
    return ImmobileEntity(
        proprietario = proprietario,
        tipoVendita = this.tipoVendita,
        categoria = this.categoria,
        indirizzo = this.indirizzo,
        mq = this.mq,
        piano = this.piano,
        ascensore = this.ascensore,
        arredamento = this.arredamento,
        climatizzazione = this.climatizzazione,
        esposizione = this.esposizione,
        statoProprieta = this.statoProprieta,
        annoCostruzione = this.annoCostruzione as LocalDate?,
        prezzo = this.prezzo,
        speseCondominiali = this.speseCondominiali,
        descrizione = this.descrizione
    )
}

fun ImmobileEntity.toDto(): ImmobileDTO {
    return ImmobileDTO(
        id = this.uuid.toString(),
        tipoVendita = this.tipoVendita,
        categoria = this.categoria,
        indirizzo = this.indirizzo,
        prezzo = this.prezzo,
        mq = this.mq,
        annoCostruzione = this.annoCostruzione?.toString(),
        descrizione = this.descrizione,
        immagini = this.immagini.map {
            ImmagineDto(it.id ?: 0, "/api/immagini/${it.id}/raw")
        },
        ambienti = this.ambienti.map {
            AmbienteDto(it.tipologia, it.numero)
        }
    )
}

// Funzione richiesta per risolvere l'errore 'Unresolved reference toSummaryDto'
// In questo caso la summary può essere uguale al DTO completo o una versione ridotta
fun ImmobileEntity.toSummaryDto(): ImmobileDTO {
    return this.toDto()
}

// --- UTENTI ---

data class UtenteRegistrazioneRequest(
    val nome: String,
    val cognome: String,
    val email: String,
    val password: String,
    val telefono: String?
)

data class UtenteResponseDTO(
    val id: String,
    val nome: String,
    val cognome: String,
    val email: String,
    val telefono: String?,
    val preferiti: List<ImmobileDTO> = emptyList()
)

data class UserUpdateRequest(
    val nome: String,
    val cognome: String,
    val email: String,
    val telefono: String,
    val password: String
)

// --- MAPPERS UTENTE ---

fun UtenteRegistrazioneRequest.toEntity(passwordEncoder: PasswordEncoder): UtenteRegistratoEntity {
    return UtenteRegistratoEntity(
        nome = this.nome,
        cognome = this.cognome,
        email = this.email,
        password = passwordEncoder.encode(this.password),
        telefono = this.telefono
    )
}

fun UserUpdateRequest.toEntity(passwordEncoder: PasswordEncoder): UtenteRegistratoEntity {
    return UtenteRegistratoEntity(
        nome = this.nome,
        cognome = this.cognome,
        email = this.email,
        telefono = this.telefono,
        password = passwordEncoder.encode(this.password),
    )
}

fun UtenteRegistratoEntity.toDto(): UtenteResponseDTO {
    return UtenteResponseDTO(
        id = this.uuid.toString(),
        nome = this.nome,
        cognome = this.cognome,
        email = this.email,
        telefono = this.telefono,
        // Qui ora 'it.toSummaryDto()' funzionerà perché definita sopra
        preferiti = this.preferiti.map { it.toSummaryDto() }
    )
}

// --- APPUNTAMENTI ---

data class AppuntamentoRequest(
    val utenteId: String,
    val immobileId: String,
    val agenteId: String,
    val data: String, // "YYYY-MM-DD"
    val orario: String // "HH:mm"
)

data class ProposalResponseRequest(
    val accettata: Boolean
)

data class AppuntamentoDTO(
    val id: String,
    val utenteId: String,
    val data: String,
    val ora: String,
    val stato: String,
    val immobileId: String?,
    val titoloImmobile: String?
)

// --- OFFERTE ---

data class OffertaRequest(
    val utenteId: String,
    val immobileId: String,
    val importo: Int,
    val corpo: String? = null
)

data class OffertaDTO(
    val id: String,
    val immobileTitolo: String,
    val importo: Int,
    val data: String
)

// --- NOTIFICHE ---

data class NotificationDTO(
    val id: String,
    val titolo: String,
    val corpo: String?,
    val data: String,
    val letto: Boolean,
    val tipo: String = "INFO"
)

data class NotificationDetailDTO(
    val id: String,
    val titolo: String,
    val corpo: String?,
    val data: String,
    val letto: Boolean,
    val tipo: String,
    val mittenteNome: String?,
    val mittenteTipo: String?,
    val isProposta: Boolean = false,
    val immobileId: String? = null,
    val prezzoProposto: Double? = null
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
    val userId: String,
    val nome: String,
    val email: String,
    val ruolo: String
)

// --- AGENZIE E AGENTI ---

data class AgenziaDTO(
    val nome: String
)

fun AgenziaEntity.toDTO(): AgenziaDTO {
    return AgenziaDTO(
        nome = this.nome
    )
}

data class AgenteDTO(
    val id: String? = null,
    val nome: String,
    val cognome: String,
    val email: String,
    val agenziaNome: String
)

fun AgenteEntity.toDTO(): AgenteDTO {
    return AgenteDTO(
        id = this.uuid.toString(),
        nome = this.nome,
        cognome = this.cognome,
        email = this.email,
        agenziaNome = this.agenzia?.nome ?: ""
    )
}
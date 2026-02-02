package com.dieti.backend.dto

import com.dieti.backend.entity.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate

// --- DTO IMMOBILE (Completo per dettagli) ---
data class ImmobileDTO(
    val id: String,
    val tipoVendita: Boolean,
    val categoria: String?,
    val indirizzo: String?,
    val prezzo: Int?,
    val mq: Int?,
    val descrizione: String?,
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

// --- DTO IMMOBILE SEMPLIFICATO (Adattato al tuo codice) ---
data class ImmobileSummaryDTO(
    val id: String,
    val prezzo: Int?,        // Adattato a Int? come nel tuo ImmobileDTO
    val indirizzo: String?,  // Adattato a String? come nel tuo ImmobileDTO
    val urlImmagine: List<ImmagineDto> // Campo calcolato utile per l'anteprima
)

// --- DTO RICHIESTA CREAZIONE IMMOBILE ---
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
    val annoCostruzione: String?,
    val prezzo: Int?,
    val speseCondominiali: Int?,
    val descrizione: String?,
    val ambienti: List<AmbienteDto> = emptyList()
)

// --- MAPPERS IMMOBILE ---

fun ImmobileCreateRequest.toEntity(proprietario: UtenteRegistratoEntity): ImmobileEntity {
    // Parsing sicuro della data
    val dataCostruzione = try {
        if (!this.annoCostruzione.isNullOrBlank()) LocalDate.parse(this.annoCostruzione) else null
    } catch (e: Exception) { null }

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
        annoCostruzione = dataCostruzione,
        prezzo = this.prezzo, // Entity usa Double, DTO usa Int
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
        prezzo = this.prezzo?.toInt(),
        mq = this.mq,
        annoCostruzione = this.annoCostruzione?.toString(),
        descrizione = this.descrizione,
        immagini = this.immagini.map {
            ImmagineDto(it.id?.toInt() ?: 0, "/api/immagini/${it.id}/raw")
        },
        ambienti = this.ambienti.map {
            AmbienteDto(it.tipologia, it.numero)
        }
    )
}

// Funzione helper per mappare l'Entity al DTO semplificato (Preferiti)
fun ImmobileEntity.toSummaryDto(): ImmobileSummaryDTO {
    // Logica per estrarre l'immagine principale: URL diretto o prima immagine della lista
    val imageToUse = this.immagini.map {
        ImmagineDto(it.id?.toInt() ?: 0, "/api/immagini/${it.id}/raw")
    }

    return ImmobileSummaryDTO(
        id = this.uuid.toString(),
        prezzo = this.prezzo?.toInt(), // Convertiamo Double a Int per uniformità
        indirizzo = this.indirizzo,
        urlImmagine = imageToUse
    )
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
    // Usiamo il DTO semplificato per la lista preferiti
    val preferiti: List<ImmobileSummaryDTO> = emptyList()
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
        // Mappiamo i preferiti usando la funzione toSummaryDto
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
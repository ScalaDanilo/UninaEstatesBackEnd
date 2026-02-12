package com.dieti.backend.dto

import com.dieti.backend.entity.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// --- DTO IMMOBILE (Completo per dettagli) ---
data class ImmobileDTO(
    val id: String,
    val tipoVendita: Boolean,
    val categoria: String?,
    val indirizzo: String?,
    val localita: String? = null,
    val prezzo: Int?,
    val mq: Int?,
    val descrizione: String?,
    val annoCostruzione: String?,

    // Nuovi campi dettagliati aggiunti per risolvere gli errori
    val piano: Int? = null,
    val ascensore: Boolean? = null,
    val arredamento: String? = null,
    val climatizzazione: Boolean? = null,
    val esposizione: String? = null,
    val statoProprieta: String? = null,
    val speseCondominiali: Int? = null,

    val immagini: List<ImmagineDto> = emptyList(),
    val ambienti: List<AmbienteDto> = emptyList(),

    // Parametri Geografici e Servizi
    val lat: Double? = null,
    val long: Double? = null,
    val parco: Boolean = false,
    val scuola: Boolean = false,
    val servizioPubblico: Boolean = false
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
    val localita: String? = "Napoli",
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

    // --- NUOVI CAMPI FONDAMENTALI PER L'ASSEGNAZIONE ---
    val lat: Double?,
    val long: Double?,

    val ambienti: List<AmbienteDto> = emptyList(),

    // Servizi
    val parco: Boolean = false,
    val scuola: Boolean = false,
    val servizioPubblico: Boolean = false
)

data class ImmagineDto(
    val id: Int,
    val url: String
)

data class AmbienteDto(
    val tipologia: String,
    val numero: Int
)

data class ImmobileSearchFilters(
    val query: String? = null,
    val tipoVendita: Boolean? = null, // true = vendita, false = affitto
    val minPrezzo: Int? = null,
    val maxPrezzo: Int? = null,
    val minMq: Int? = null,
    val maxMq: Int? = null,
    val minStanze: Int? = null,
    val maxStanze: Int? = null,
    val bagni: Int? = null,
    val condizione: String? = null,

    // CAMPI AGGIUNTI PER FIXARE L'ERRORE NEL SERVICE
    val lat: Double? = null,
    val lon: Double? = null,
    val radiusKm: Double? = null
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
        },
        // MAPPING CORRETTO: Entity (Inglese) -> DTO (Italiano)
        lat = this.lat,
        long = this.long,
        scuola = this.scuola,
        parco = this.parco,
        servizioPubblico = this.servizioPubblico
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

// --- MODIFICA: Aggiunto campo 'ruolo' ---
data class UtenteResponseDTO(
    val id: String,
    val nome: String,
    val cognome: String,
    val email: String,
    val telefono: String?,
    val ruolo: String, // "UTENTE" o "MANAGER"

    // Lista preferiti (popolata solo se UTENTE)
    val preferiti: List<ImmobileSummaryDTO> = emptyList(),

    // Campo specifico per MANAGER (nullo se UTENTE)
    val agenziaNome: String? = null
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
        ruolo = "UTENTE",
        // Mappiamo i preferiti usando la funzione toSummaryDto
        preferiti = this.preferiti.map { it.toSummaryDto() }
    )
}

// --- APPUNTAMENTI ---

data class AppuntamentoRequest(
    val utenteId: String,
    val immobileId: String,
    val agenteId: String,
    val data: String,
    val orario: String
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
// FIX: Aggiunto qui per risolvere "Unresolved reference"
data class CreateAgenziaRequest(
    val nome: String,
    val indirizzo: String,
    val adminId: String // String per facilitare il passaggio dal JSON
)



// DTO per creare un Amministratore (semplificato)
// DTO per il cambio password
data class ChangePasswordRequest(
    val adminId: UUID,
    val oldPassword: String,
    val newPassword: String
)

// DTO di risposta Agente
data class AgenteDTO(
    val id: String,
    val nome: String,
    val cognome: String,
    val email: String,
    val isCapo: Boolean,
    val agenziaNome: String
)
// NUOVO: Risposta login admin
data class AdminLoginResponse(
    val id: String,
    val email: String,
    val role: String
)

// --- MAPPER AGENTE (Ruolo = MANAGER) ---
// Usiamo questo per il Login, così il Frontend riceve lo stesso tipo di oggetto
fun AgenteEntity.toDto(): UtenteResponseDTO {
    return UtenteResponseDTO(
        id = this.uuid.toString(),
        nome = this.nome,
        cognome = this.cognome,
        email = this.email,
        telefono = null,
        ruolo = "MANAGER",
        preferiti = emptyList(),
        // Qui mappiamo il nome dell'agenzia
        agenziaNome = this.agenzia.nome
    )
}

// --- MAPPER SPECIFICO AGENTE ---
// Usiamo questo per il controller specifico AgenteController (GET /api/agenti/{id})
fun AgenteEntity.toDTO(): AgenteDTO {
    return AgenteDTO(
        id = this.uuid.toString(),
        nome = this.nome,
        cognome = this.cognome,
        email = this.email,
        agenziaNome = this.agenzia.nome,
        isCapo = this.isCapo
    )
}


// DTO per popolare la dropdown delle agenzie nel frontend
data class AgenziaOptionDTO(
    val id: String,
    val nome: String,
    val haCapo: Boolean
)

// DTO per popolare la dropdown degli amministratori
data class AdminOptionDTO(
    val id: String,
    val email: String
)

data class CreateAdminRequest(
    val email: String,
    val password: String
)

data class CreateAgenteRequest(
    val nome: String,
    val cognome: String,
    val email: String,
    val password: String,
    val agenziaId: String,
    val isCapo: Boolean
)

data class ChangeMyPasswordRequest(
    val adminId: String? = null,
    val oldPassword: String,
    val newPassword: String
)

// NUOVO: DTO completo per l'Agenzia (evita il problema LazyInitialization)
data class AgenziaDTO(
    val id: String,
    val nome: String,
    val indirizzo: String,
    val lat: Double,
    val long: Double,
    // MODIFICA: Restituiamo l'ID dell'admin, più utile per riferimenti programmatici
    val adminId: String?
)

data class OffertaRicevutaDTO(
    val id: String,
    val nomeOfferente: String,
    val cognomeOfferente: String,
    val prezzoOfferto: Int,
    val immobileId: String,
    val immobileTitolo: String, // Indirizzo o Località
    val immobilePrezzoBase: Int,
    val immagineUrl: String?,
    val dataOfferta: LocalDateTime // Spring Boot lo serializzerà in ISO String automaticamente
)

data class RispostaRequest(
    val offertaId: String,
    val venditoreId: String, // L'agente che risponde
    val esito: String, // "ACCETTATA", "RIFIUTATA", "CONTROPROPOSTA"
    val nuovoPrezzo: Int? = null, // Solo se controproposta
    val messaggio: String? = null
)

data class RichiestaDTO(
    val id: String,
    val titolo: String,
    val descrizione: String?,
    val data: String,
    val stato: String, // "PENDING", "ACCEPTED", etc.
    val immagineUrl: String? = null // AGGIUNTO per mostrare la foto nella lista notifiche
)

data class EsitoRichiestaRequest(
    val id: String
)

data class ManagerDashboardStats(
    val numeroNotifiche: Int,
    val numeroProposte: Int
)

// DTO per la lista e il dettaglio rapido
data class TrattativaSummaryDTO(
    val offertaId: String,
    val immobileId: String, // NUOVO
    val immobileTitolo: String,
    val immobileIndirizzo: String?,
    val prezzoOfferto: Int, // NUOVO
    val nomeOfferente: String, // NUOVO
    val ultimoStato: String,
    val ultimaModifica: String,
    val immagineUrl: String?
)

data class StoriaTrattativaDTO(
    val offertaId: String,
    val prezzoIniziale: Int,
    val immobileTitolo: String,
    val cronologia: List<MessaggioTrattativaDTO>,
    val canUserReply: Boolean
)

data class MessaggioTrattativaDTO(
    val autoreNome: String,
    val isMe: Boolean,
    val testo: String,
    val prezzo: Int?,
    val tipo: String,
    val data: String
)

data class UserResponseRequest(
    val offertaId: String,
    val utenteId: String,
    val esito: String,
    val nuovoPrezzo: Int? = null,
    val messaggio: String? = null
)

data class NotificaDTO(
    val id: String,
    val titolo: String,
    val corpo: String,
    val data: String, // O LocalDateTime se gestito dal serializer
    val letto: Boolean
)


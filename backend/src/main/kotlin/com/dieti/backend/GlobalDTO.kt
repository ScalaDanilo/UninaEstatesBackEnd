package com.dieti.backend

import com.dieti.backend.entity.AgenteEntity
import com.dieti.backend.entity.AgenziaEntity
import com.dieti.backend.entity.AmbienteEntity
import com.dieti.backend.entity.ImmagineEntity
import com.dieti.backend.entity.ImmobileEntity
import com.dieti.backend.entity.UtenteRegistratoEntity
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate
import kotlin.Int


// --- IMMAGINI & AMBIENTE---

data class ImmagineDTO(
    val id: Int,
    val nome: String?,
    val formato: String?,
    val url: String? = null
)
fun ImmagineEntity.toDto(): ImmagineDTO {
    val idImmagine = this.id ?: 0
    return ImmagineDTO(
        id = idImmagine,
        nome = this.nome,
        formato = this.formato,
        // Esempio: crei già l'URL che il frontend dovrà chiamare per vedere la foto
        url = "/api/immagini/$idImmagine/raw"
    )
}

data class AmbienteDTO(
    val tipologia: String,
    val numero: Int
)
fun AmbienteEntity.toDto(): AmbienteDTO {
    return AmbienteDTO(
        tipologia = this.tipologia ?: "",
        numero = this.numero ?: 0
    )
}

/// --- IMOBILI ---

data class ImmobileDTO(
    val id: String? = null, // UUID come String
    val tipoVendita: Boolean,
    val categoria: String? = null,
    val tipologia: String? = null,
    val localita: String? = null,
    val mq: Int? = null,
    val piano: Int? = null,
    val ascensore: Boolean? = null,
    val dettagli: String? = null,
    val arredamento: String? = null,
    val climatizzazione: Boolean? = null,
    val esposizione: String? = null,
    val tipoProprieta: String? = null,
    val statoProprieta: String? = null,
    val annoCostruzione: LocalDate? = null,
    val prezzo: Int? = null,
    val speseCondominiali: Int? = null,
    val disponibilita: Boolean = true,
    val descrizione: String? = null,

    val proprietario: UtenteResponseDTO,
    val immagini: List<ImmagineDTO> = emptyList(),
    val ambienti: List<AmbienteDTO> = emptyList()
)
fun ImmobileEntity.toDto(): ImmobileDTO {
    return ImmobileDTO(
        id = this.uuid.toString(),
        tipoVendita = this.tipoVendita,
        categoria = this.categoria,
        tipologia = this.tipologia,
        localita = this.localita,
        mq = this.mq,
        piano = this.piano,
        ascensore = this.ascensore,
        dettagli = this.dettagli,
        arredamento = this.arredamento,
        climatizzazione = this.climatizzazione,
        esposizione = this.esposizione,
        tipoProprieta = this.tipoProprieta,
        statoProprieta = this.statoProprieta,
        annoCostruzione = this.annoCostruzione,
        prezzo = this.prezzo,
        speseCondominiali = this.speseCondominiali,
        disponibilita = this.disponibilita,
        descrizione = this.descrizione,

        // QUI AVVIENE LA MAGIA: converte ogni elemento della lista
        proprietario = this.proprietario.toDto(),
        immagini = this.immagini.map { it.toDto() },
        ambienti = this.ambienti.map { it.toDto() }
    )
}
data class ImmobileSummaryDTO(
    val id: String? = null, // UUID come String
    val tipoVendita: Boolean,
    val categoria: String? = null,
    val tipologia: String? = null,
    val localita: String? = null,
    val mq: Int? = null,
    val piano: Int? = null,
    val ascensore: Boolean? = null,
    val dettagli: String? = null,
    val arredamento: String? = null,
    val climatizzazione: Boolean? = null,
    val esposizione: String? = null,
    val tipoProprieta: String? = null,
    val statoProprieta: String? = null,
    val annoCostruzione: LocalDate? = null,
    val prezzo: Int? = null,
    val speseCondominiali: Int? = null,
    val disponibilita: Boolean = true,
    val descrizione: String? = null,

    val immagini: List<ImmagineDTO> = emptyList(),
    val ambienti: List<AmbienteDTO> = emptyList()
)
fun ImmobileEntity.toSummaryDto(): ImmobileSummaryDTO {
    return ImmobileSummaryDTO(
        id = this.uuid.toString(),
        tipoVendita = this.tipoVendita,
        categoria = this.categoria,
        tipologia = this.tipologia,
        localita = this.localita,
        mq = this.mq,
        piano = this.piano,
        ascensore = this.ascensore,
        dettagli = this.dettagli,
        arredamento = this.arredamento,
        climatizzazione = this.climatizzazione,
        esposizione = this.esposizione,
        tipoProprieta = this.tipoProprieta,
        statoProprieta = this.statoProprieta,
        annoCostruzione = this.annoCostruzione,
        prezzo = this.prezzo,
        speseCondominiali = this.speseCondominiali,
        disponibilita = this.disponibilita,
        descrizione = this.descrizione,

        immagini = this.immagini.map { it.toDto() },
        ambienti = this.ambienti.map { it.toDto() }
    )
}
data class ImmobileCreateRequest(
    val tipoVendita: Boolean,
    val categoria: String? = null,
    val tipologia: String? = null,
    val localita: String? = null,
    val mq: Int? = null,
    val piano: Int? = null,
    val ascensore: Boolean? = null,
    val dettagli: String? = null,
    val arredamento: String? = null,
    val climatizzazione: Boolean? = null,
    val esposizione: String? = null,
    val tipoProprieta: String? = null,
    val statoProprieta: String? = null,
    val annoCostruzione: LocalDate? = null,
    val prezzo: Int? = null,
    val speseCondominiali: Int? = null,
    val disponibilita: Boolean = true,
    val descrizione: String? = null,
    val ambienti: List<AmbienteDTO> = emptyList()
)
fun ImmobileCreateRequest.toEntity(proprietario: UtenteRegistratoEntity): ImmobileEntity {

    val immobileEntity = ImmobileEntity(
        proprietario = proprietario,
        tipoVendita = this.tipoVendita,
        mq = this.mq,
        categoria = this.categoria,
        tipologia = this.tipologia,
        localita = this.localita,
        piano = this.piano,
        ascensore = this.ascensore,
        dettagli = this.dettagli,
        arredamento = this.arredamento,
        climatizzazione = this.climatizzazione,
        esposizione = this.esposizione,
        tipoProprieta = this.tipoProprieta,
        statoProprieta = this.statoProprieta,
        annoCostruzione = this.annoCostruzione,
        prezzo = this.prezzo,
        speseCondominiali = this.speseCondominiali,
        disponibilita = this.disponibilita,
        descrizione = this.descrizione,
    )

    // 2. Gestiamo gli AMBIENTI (Figli)
    // Dobbiamo trasformare anche i DTO degli ambienti in Entità Ambienti
    val listaAmbientiEntity = this.ambienti.map { AmbienteDTO ->
        AmbienteEntity(
            immobile = immobileEntity,
            tipologia = AmbienteDTO.tipologia,
            numero = AmbienteDTO.numero
        )
    }.toMutableList()

    // 3. Assegniamo la lista all'immobile (se il tuo ImmobileEntity lo prevede modificabile)
    // Nota: Spesso le liste @OneToMany sono delicate, ma nel costruttore o via setter si fa così:
    // immobileEntity.ambienti.addAll(listaAmbientiEntity)
    // Oppure se è nel costruttore come nel tuo codice precedente, devi farlo prima.

    return immobileEntity.apply {
        // Se usi MutableList dentro l'Entity, aggiungili qui
        this.ambienti.addAll(listaAmbientiEntity)
    }
}

// --- UTENTI ---
data class UtenteRegistrazioneRequest(
    val nome: String,
    val cognome: String,
    val email: String,
    val password: String,
    val telefono: String?
)

fun UtenteRegistrazioneRequest.toEntity(passwordEncoder: PasswordEncoder): UtenteRegistratoEntity {
    return UtenteRegistratoEntity(
        nome = this.nome,
        cognome = this.cognome,
        email = this.email,
        password = passwordEncoder.encode(this.password),
        telefono = this.telefono
    )
}

data class UtenteResponseDTO(
    val id: String,
    val nome: String,
    val cognome: String,
    val email: String,
    val telefono: String?,

    val preferiti: List<ImmobileSummaryDTO> = emptyList()
)
fun UtenteRegistratoEntity.toDto(): UtenteResponseDTO {
    return UtenteResponseDTO(
        id = this.uuid.toString(),
        nome = this.nome,
        cognome = this.cognome,
        email = this.email,
        telefono = this.telefono,
        preferiti = this.preferiti.map { it.toSummaryDto() }
    )
}


data class UserUpdateRequest(
    val nome: String,
    val cognome: String,
    val email: String,
    val telefono: String,
    val password: String
)

fun UserUpdateRequest.toEntity(passwordEncoder: PasswordEncoder): UtenteRegistratoEntity {
    return UtenteRegistratoEntity(
        nome = this.nome,
        cognome = this.cognome,
        email = this.email,
        telefono = this.telefono,
        password = passwordEncoder.encode(this.password),
    )
}


// --- APPUNTAMENTI ---
data class AppuntamentoRequest(
    val utenteId: String,
    val immobileId: String,
    val agenteId: String,
    val data: String, // Formato "YYYY-MM-DD"
    val orario: String // Formato "HH:mm"
)

data class ProposalResponseRequest(
    val accettata: Boolean
)

data class AppuntamentoDTO(
    val id: String,
    val utenteId: String,
    val data: String, // "YYYY-MM-DD"
    val ora: String,  // "HH:mm"
    val stato: String, // "PROGRAMMATO", "COMPLETATO", "CANCELLATO"
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
    val data: String, // ISO date string
    val letto: Boolean,
    val tipo: String = "INFO"
)

data class NotificationDetailDTO(
    val id: String, // UUID
    val titolo: String,
    val corpo: String?,
    val data: String,
    val letto: Boolean,
    val tipo: String,
    val mittenteNome: String?,
    val mittenteTipo: String?,
    val isProposta: Boolean = false,
    val immobileId: String? = null, // UUID
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

fun AgenteEntity.toDTO(): AgenteDTO{
    return AgenteDTO (
        id = this.uuid.toString(),
        nome = this.nome,
        cognome = this.cognome,
        email = this.email,
        agenziaNome = this.agenzia?.nome ?: ""
    )
}
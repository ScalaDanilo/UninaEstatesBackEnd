package com.dieti.backend.service

import com.dieti.backend.dto.RispostaRequest
import com.dieti.backend.dto.UserResponseRequest
import com.dieti.backend.entity.RispostaEntity
import com.dieti.backend.repository.OffertaRepository
import com.dieti.backend.repository.RispostaRepository
import com.dieti.backend.repository.UtenteRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class RispostaService(
    private val rispostaRepository: RispostaRepository,
    private val offertaRepository: OffertaRepository,
    private val utenteRepository: UtenteRepository,
    private val notificaService: NotificaService
) {

    // --- RISPOSTA DEL MANAGER ---
    @Transactional
    fun creaRispostaManager(request: RispostaRequest): RispostaEntity {
        val offerta = offertaRepository.findById(UUID.fromString(request.offertaId))
            .orElseThrow { EntityNotFoundException("Offerta non trovata") }

        // 1. Controllo di Sicurezza: Chi sta facendo la richiesta?
        // L'ID passato nel request.venditoreId è quello dell'AGENTE loggato.
        // Verifichiamo che questo agente sia quello che gestisce l'immobile.
        val idAgenteImmobile = offerta.immobile.agente?.uuid?.toString()
        if (idAgenteImmobile != request.venditoreId) {
            throw IllegalArgumentException("Non sei l'agente autorizzato per questo immobile.")
        }

        // 2. Creazione Risposta
        // IMPORTANTE: Nel DB, la colonna 'venditore_id' punta alla tabella utenti (proprietari).
        // L'Agente agisce "in nome e per conto" del Venditore.
        // Quindi il MITTENTE formale nel DB è il Proprietario (offerta.venditore),
        // anche se l'azione è stata scatenata dall'Agente.
        val risposta = RispostaEntity(
            offerta = offerta,
            mittente = offerta.venditore, // Usiamo il proprietario come mittente formale per rispettare la FK
            destinatario = offerta.offerente,
            tipo = request.esito,
            prezzoProposto = if (request.esito == "CONTROPROPOSTA") request.nuovoPrezzo else null,
            corpo = request.messaggio
        )

        val saved = rispostaRepository.save(risposta)

        // 3. Notifica all'Utente
        val dataApp = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("dd/MM"))
        val (titolo, corpo) = when (request.esito) {
            "ACCETTATA" -> "Offerta Accettata! 🎉" to "L'agente ha accettato la tua offerta per ${offerta.immobile.localita}. Ci vediamo il $dataApp per i dettagli."
            "RIFIUTATA" -> "Offerta Rifiutata" to "La tua proposta per ${offerta.immobile.localita} non è stata accettata."
            "CONTROPROPOSTA" -> "Nuova Controproposta" to "L'agente ha proposto €${request.nuovoPrezzo}. Vai nella sezione Offerte per rispondere."
            else -> "Aggiornamento Trattativa" to "Nuovo messaggio dall'agente."
        }

        notificaService.inviaNotifica(offerta.offerente, titolo, corpo, "TRATTATIVA")

        return saved
    }

    // --- RISPOSTA DELL'UTENTE ---
    @Transactional
    fun creaRispostaUtente(request: UserResponseRequest): RispostaEntity {
        val offerta = offertaRepository.findById(UUID.fromString(request.offertaId))
            .orElseThrow { EntityNotFoundException("Offerta non trovata") }

        val utente = utenteRepository.findById(UUID.fromString(request.utenteId))
            .orElseThrow { RuntimeException("Utente non trovato") }

        val risposta = RispostaEntity(
            offerta = offerta,
            mittente = utente,
            destinatario = offerta.venditore,
            tipo = request.esito,
            prezzoProposto = if (request.esito == "CONTROPROPOSTA") request.nuovoPrezzo else null,
            corpo = request.messaggio
        )

        return rispostaRepository.save(risposta)
    }
}
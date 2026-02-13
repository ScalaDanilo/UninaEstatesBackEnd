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
    private val notificaService: NotificaService,
    private val firebaseService: FirebaseNotificationService // NUOVO
) {

    // --- RISPOSTA DEL MANAGER ---
    @Transactional
    fun creaRispostaManager(request: RispostaRequest): RispostaEntity {
        val offerta = offertaRepository.findById(UUID.fromString(request.offertaId))
            .orElseThrow { EntityNotFoundException("Offerta non trovata") }

        val idAgenteImmobile = offerta.immobile.agente?.uuid?.toString()
        if (idAgenteImmobile != request.venditoreId) {
            throw IllegalArgumentException("Non sei l'agente autorizzato per questo immobile.")
        }

        val risposta = RispostaEntity(
            offerta = offerta,
            mittente = offerta.venditore,
            destinatario = offerta.offerente,
            tipo = request.esito,
            prezzoProposto = if (request.esito == "CONTROPROPOSTA") request.nuovoPrezzo else null,
            corpo = request.messaggio
        )

        val saved = rispostaRepository.save(risposta)

        // Logica testo notifica
        val dataApp = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("dd/MM"))
        val (titolo, corpo) = when (request.esito) {
            "ACCETTATA" -> "Offerta Accettata! 🎉" to "L'agente ha accettato la tua offerta per ${offerta.immobile.localita}."
            "RIFIUTATA" -> "Offerta Rifiutata" to "La tua proposta per ${offerta.immobile.localita} non è stata accettata."
            "CONTROPROPOSTA" -> "Nuova Controproposta" to "L'agente ha proposto €${request.nuovoPrezzo}. Rispondi subito!"
            else -> "Aggiornamento Trattativa" to "Nuovo messaggio dall'agente."
        }

        // Notifica DB
        notificaService.inviaNotifica(offerta.offerente, titolo, corpo, "TRATTATIVA")

        // Notifica Push (Se abilitata nelle impostazioni)
        firebaseService.sendNotificationToUser(offerta.offerente, titolo, corpo) { it.notifTrattative }

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
            destinatario = offerta.venditore, // Nota: Questo è il proprietario DB, ma l'app manager la vedrà
            tipo = request.esito,
            prezzoProposto = if (request.esito == "CONTROPROPOSTA") request.nuovoPrezzo else null,
            corpo = request.messaggio
        )

        // Qui non inviamo notifica Push al Manager (Agente) perché il manager è un utente speciale
        // e solitamente usa la dashboard, ma potremmo implementarlo se l'Agente avesse un token FCM.

        return rispostaRepository.save(risposta)
    }
}
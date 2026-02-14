package com.dieti.backend.service

import com.dieti.backend.dto.RispostaRequest
import com.dieti.backend.dto.UserResponseRequest
import com.dieti.backend.entity.RispostaEntity
import com.dieti.backend.repository.OffertaRepository
import com.dieti.backend.repository.RispostaRepository
import com.dieti.backend.repository.UtenteRepository
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
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
    private val firebaseService: FirebaseNotificationService
) {

    private val logger = LoggerFactory.getLogger(RispostaService::class.java)

    // --- RISPOSTA DEL MANAGER ---
    @Transactional
    fun creaRispostaManager(request: RispostaRequest): RispostaEntity {
        logger.info(">>> [DEBUG NOTIFICHE] Inizio creaRispostaManager per offerta: ${request.offertaId}")

        val offerta = offertaRepository.findById(UUID.fromString(request.offertaId))
            .orElseThrow { EntityNotFoundException("Offerta non trovata") }

        val idAgenteImmobile = offerta.immobile.agente?.uuid?.toString()
        if (idAgenteImmobile != request.venditoreId) {
            logger.error(">>> [DEBUG NOTIFICHE] ERRORE: Agente non autorizzato.")
            throw IllegalArgumentException("Non sei l'agente autorizzato per questo immobile.")
        }

        val risposta = RispostaEntity(
            offerta = offerta,
            mittente = offerta.venditore, // NOTA: nel DB venditore = proprietario, ma logicamente risponde l'agente per conto suo
            destinatario = offerta.offerente,
            tipo = request.esito,
            prezzoProposto = if (request.esito == "CONTROPROPOSTA") request.nuovoPrezzo else null,
            corpo = request.messaggio
        )

        val saved = rispostaRepository.save(risposta)
        logger.info(">>> [DEBUG NOTIFICHE] Risposta salvata su DB.")

        // Logica testo notifica
        val (titolo, corpo) = when (request.esito) {
            "ACCETTATA" -> "Offerta Accettata! 🎉" to "Complimenti! La tua offerta per ${offerta.immobile.localita} è stata accettata."
            "RIFIUTATA" -> "Offerta Rifiutata ✋" to "La tua proposta per ${offerta.immobile.localita} non è stata accettata."
            "CONTROPROPOSTA" -> "Nuova Controproposta 🔄" to "L'agente ha proposto €${request.nuovoPrezzo}. Controlla subito!"
            else -> "Aggiornamento Trattativa" to "Nuovo messaggio dall'agente."
        }

        // 1. Notifica DB (Persistenza)
        notificaService.inviaNotifica(offerta.offerente, titolo, corpo, "TRATTATIVA")
        logger.info(">>> [DEBUG NOTIFICHE] Notifica salvata nella tabella 'Notifica'.")

        // 2. Notifica Push (Immediata)
        val destinatario = offerta.offerente
        logger.info(">>> [DEBUG NOTIFICHE] Preparazione Push per utente: ${destinatario.email}")
        logger.info(">>> [DEBUG NOTIFICHE] Stato Utente -> Ha Token: ${!destinatario.fcmToken.isNullOrBlank()}, Pref. Trattative: ${destinatario.notifTrattative}")

        // Controlla la preferenza 'notifTrattative' dell'utente
        firebaseService.sendNotificationToUser(destinatario, titolo, corpo) {
            val shouldSend = it.notifTrattative
            if (!shouldSend) logger.warn(">>> [DEBUG NOTIFICHE] SKIP PUSH: L'utente ha disabilitato le notifiche trattative.")
            shouldSend
        }

        return saved
    }

    // --- RISPOSTA DELL'UTENTE ---
    @Transactional
    fun creaRispostaUtente(request: UserResponseRequest): RispostaEntity {
        logger.info(">>> [DEBUG NOTIFICHE] creaRispostaUtente invocato per offerta: ${request.offertaId}")

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

        // Nota: Se volessi notificare l'agente (Manager), dovresti avere il token FCM dell'agente.
        // Attualmente il sistema notifica solo gli Utenti finali.
        logger.info(">>> [DEBUG NOTIFICHE] Risposta utente salvata. Nessuna push inviata all'agente (by design).")

        return rispostaRepository.save(risposta)
    }
}
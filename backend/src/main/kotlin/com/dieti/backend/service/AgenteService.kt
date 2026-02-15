package com.dieti.backend.service

import com.dieti.backend.dto.AgenteDTO
import com.dieti.backend.dto.CreateAgenteRequest
import com.dieti.backend.dto.ImmobileDTO
import com.dieti.backend.dto.toDTO
import com.dieti.backend.dto.toDto
import com.dieti.backend.entity.AgenteEntity
import com.dieti.backend.repository.AgenteRepository
import com.dieti.backend.repository.AgenziaRepository
import com.dieti.backend.repository.ImmobileRepository
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import java.util.concurrent.CompletableFuture

@Service
class AgenteService(
    private val agenteRepository: AgenteRepository,
    private val agenziaRepository: AgenziaRepository,
    private val immobileRepository: ImmobileRepository,
    private val notificaService: NotificaService,
    private val firebaseService: FirebaseNotificationService,
    private val passwordEncoder: PasswordEncoder
) {
    private val logger = LoggerFactory.getLogger(AgenteService::class.java)

    // ... (metodi creaAgente, getAgenteById, getAllAgenti, getRichiestePendenti rimangono uguali) ...
    @Transactional
    fun creaAgente(request: CreateAgenteRequest): AgenteDTO {
        val agenziaUUID = UUID.fromString(request.agenziaId)
        val agenzia = agenziaRepository.findById(agenziaUUID)
            .orElseThrow { RuntimeException("Agenzia non trovata") }

        if (request.isCapo && agenzia.agenti.any { it.isCapo }) {
            throw RuntimeException("Questa agenzia ha già un capo.")
        }

        val nuovoAgente = AgenteEntity(
            nome = request.nome,
            cognome = request.cognome,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            agenzia = agenzia,
            isCapo = request.isCapo
        )

        val salvato = agenteRepository.save(nuovoAgente)
        return salvato.toDTO()
    }

    @Transactional(readOnly = true)
    fun getAgenteById(uuid: UUID): AgenteEntity {
        return agenteRepository.findById(uuid)
            .orElseThrow { RuntimeException("Agente con ID $uuid non trovato") }
    }

    @Transactional(readOnly = true)
    fun getAllAgenti(): List<AgenteEntity> {
        return agenteRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getRichiestePendenti(agenteEmail: String): List<ImmobileDTO> {
        val agente = agenteRepository.findByEmail(agenteEmail) ?: throw EntityNotFoundException("Agente non trovato")
        val agenziaId = agente.agenzia.uuid ?: throw EntityNotFoundException("ID Agenzia è NULL")
        val immobili = immobileRepository.findRichiestePendentiPerAgenzia(agenziaId)
        return immobili.map { it.toDto() }
    }

    // --- ACCETTA INCARICO + NOTIFICHE ASINCRONE ---
    @Transactional
    fun accettaIncarico(agenteEmail: String, immobileId: String) {
        val agente = agenteRepository.findByEmail(agenteEmail)
            ?: throw EntityNotFoundException("Agente non trovato")

        val uuidImmobile = UUID.fromString(immobileId)
        val immobile = immobileRepository.findById(uuidImmobile)
            .orElseThrow { EntityNotFoundException("Immobile non trovato") }

        if (immobile.agenzia?.uuid != agente.agenzia.uuid) {
            throw RuntimeException("Questo immobile non appartiene alla tua agenzia")
        }

        // Assegna l'agente all'immobile
        immobile.agente = agente
        val saved = immobileRepository.save(immobile)

        // FIX CRITICO: Eseguiamo le chiamate esterne (Firebase) in un thread separato
        // per evitare che il client Android vada in timeout aspettando la risposta HTTP.
        CompletableFuture.runAsync {
            try {
                // 1. NOTIFICA AL PROPRIETARIO (Richiesta Accettata)
                val titoloOwner = "Immobile Pubblicato! 🏠"
                val corpoOwner = "Il tuo immobile in ${saved.localita} è stato accettato e pubblicato dall'agente ${agente.nome}."

                // Salvataggio su DB (meglio farlo qui o tenerlo sincrono se veloce, ma qui va bene)
                notificaService.inviaNotifica(saved.proprietario, titoloOwner, corpoOwner, "SISTEMA")

                // Push Notification (Lenta)
                firebaseService.sendNotificationToUser(saved.proprietario, titoloOwner, corpoOwner) {
                    it.notifPubblicazione
                }

                // 2. NOTIFICA AGLI UTENTI INTERESSATI
                if (!saved.localita.isNullOrBlank()) {
                    val indirizzoCompleto = if(saved.indirizzo.isNullOrBlank()) saved.localita!! else "${saved.localita}, ${saved.indirizzo}"
                    firebaseService.notifyUsersForNewProperty(saved.localita!!, indirizzoCompleto)
                }
            } catch (e: Exception) {
                logger.error("Errore invio notifiche asincrone: ${e.message}")
            }
        }
    }

    // --- RIFIUTA INCARICO + NOTIFICHE ASINCRONE ---
    @Transactional
    fun rifiutaIncarico(agenteEmail: String, immobileId: String) {
        val agente = agenteRepository.findByEmail(agenteEmail)
            ?: throw EntityNotFoundException("Agente non trovato")

        val immobile = immobileRepository.findById(UUID.fromString(immobileId))
            .orElseThrow { EntityNotFoundException("Immobile non trovato") }

        if (immobile.agenzia?.uuid != agente.agenzia.uuid) {
            throw RuntimeException("Questo immobile non appartiene alla tua agenzia")
        }

        // Recuperiamo i dati necessari per la notifica PRIMA di cancellare l'immobile
        val proprietario = immobile.proprietario
        val localita = immobile.localita

        // Cancellazione DB
        immobileRepository.delete(immobile)

        // FIX CRITICO: Notifiche Asincrone
        CompletableFuture.runAsync {
            try {
                val titolo = "Richiesta Rifiutata ❌"
                val corpo = "Ci dispiace, la richiesta per $localita non è stata accettata dall'agenzia."

                notificaService.inviaNotifica(proprietario, titolo, corpo)
                firebaseService.sendNotificationToUser(proprietario, titolo, corpo) {
                    it.notifPubblicazione
                }
            } catch (e: Exception) {
                logger.error("Errore invio notifiche rifiuto: ${e.message}")
            }
        }
    }
}
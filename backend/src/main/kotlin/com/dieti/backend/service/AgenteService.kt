package com.dieti.backend.service

import com.dieti.backend.dto.AgenteDTO
import com.dieti.backend.dto.CreateAgenteRequest
import com.dieti.backend.dto.CreateSubAgentRequest
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

        immobile.agente = agente
        val saved = immobileRepository.save(immobile)

        CompletableFuture.runAsync {
            try {
                val titoloOwner = "Immobile Pubblicato! 🏠"
                val corpoOwner = "Il tuo immobile in ${saved.localita} è stato accettato e pubblicato dall'agente ${agente.nome}."

                notificaService.inviaNotifica(saved.proprietario, titoloOwner, corpoOwner, "SISTEMA")

                firebaseService.sendNotificationToUser(saved.proprietario, titoloOwner, corpoOwner) {
                    it.notifPubblicazione
                }

                if (!saved.localita.isNullOrBlank()) {
                    val indirizzoCompleto = if(saved.indirizzo.isNullOrBlank()) saved.localita!! else "${saved.localita}, ${saved.indirizzo}"
                    firebaseService.notifyUsersForNewProperty(saved.localita!!, indirizzoCompleto)
                }
            } catch (e: Exception) {
                logger.error("Errore invio notifiche asincrone: ${e.message}")
            }
        }
    }

    @Transactional
    fun rifiutaIncarico(agenteEmail: String, immobileId: String) {
        val agente = agenteRepository.findByEmail(agenteEmail)
            ?: throw EntityNotFoundException("Agente non trovato")

        val immobile = immobileRepository.findById(UUID.fromString(immobileId))
            .orElseThrow { EntityNotFoundException("Immobile non trovato") }

        if (immobile.agenzia?.uuid != agente.agenzia.uuid) {
            throw RuntimeException("Questo immobile non appartiene alla tua agenzia")
        }

        val proprietario = immobile.proprietario
        val localita = immobile.localita

        immobileRepository.delete(immobile)

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

    fun creaSottoAgente(managerId: String, req: CreateSubAgentRequest) {
        val manager = agenteRepository.findById(UUID.fromString(managerId))
            .orElseThrow { RuntimeException("Manager non trovato") }

        if (!manager.isCapo) throw RuntimeException("Non hai i permessi di Capo Agenzia")

        val nuovaEntity = AgenteEntity(
            nome = req.nome,
            cognome = req.cognome,
            email = req.email,
            password = passwordEncoder.encode(req.password),
            agenzia = manager.agenzia,
            isCapo = false
        )
        agenteRepository.save(nuovaEntity)
    }
}
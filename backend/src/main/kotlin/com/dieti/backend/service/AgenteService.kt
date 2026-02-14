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
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AgenteService(
    private val agenteRepository: AgenteRepository,
    private val agenziaRepository: AgenziaRepository,
    private val immobileRepository: ImmobileRepository,
    private val notificaService: NotificaService,
    private val firebaseService: FirebaseNotificationService,
    private val passwordEncoder: PasswordEncoder
) {

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


    // --- ACCETTA INCARICO + NOTIFICHE ---
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

        // 1. NOTIFICA AL PROPRIETARIO (Richiesta Accettata)
        // Viene inviata SOLO se notifPubblicazione è TRUE nel DB
        val titoloOwner = "Immobile Pubblicato! 🏠"
        val corpoOwner = "Il tuo immobile in ${saved.localita} è stato accettato e pubblicato dall'agente ${agente.nome}."

        notificaService.inviaNotifica(saved.proprietario, titoloOwner, corpoOwner, "SISTEMA")
        firebaseService.sendNotificationToUser(saved.proprietario, titoloOwner, corpoOwner) {
            it.notifPubblicazione // Check Preferenza
        }

        // 2. NOTIFICA AGLI UTENTI INTERESSATI (Nuovo Immobile in zona)
        // Cerca utenti con ricerche salvate in questa zona e invia notifica
        // Controlla automaticamente notifNuoviImmobili
        if (!saved.localita.isNullOrBlank()) {
            val indirizzoCompleto = if(saved.indirizzo.isNullOrBlank()) saved.localita!! else "${saved.localita}, ${saved.indirizzo}"
            firebaseService.notifyUsersForNewProperty(saved.localita!!, indirizzoCompleto)
        }
    }

    // --- RIFIUTA INCARICO + NOTIFICHE ---
    @Transactional
    fun rifiutaIncarico(agenteEmail: String, immobileId: String) {
        val agente = agenteRepository.findByEmail(agenteEmail)
            ?: throw EntityNotFoundException("Agente non trovato")

        val immobile = immobileRepository.findById(UUID.fromString(immobileId))
            .orElseThrow { EntityNotFoundException("Immobile non trovato") }

        if (immobile.agenzia?.uuid != agente.agenzia.uuid) {
            throw RuntimeException("Questo immobile non appartiene alla tua agenzia")
        }

        // 1. NOTIFICA AL PROPRIETARIO (Richiesta Rifiutata)
        val titolo = "Richiesta Rifiutata ❌"
        val corpo = "Ci dispiace, la richiesta per ${immobile.localita} non è stata accettata dall'agenzia."

        notificaService.inviaNotifica(immobile.proprietario, titolo, corpo)
        firebaseService.sendNotificationToUser(immobile.proprietario, titolo, corpo) {
            it.notifPubblicazione
        }

        immobileRepository.delete(immobile)
    }
}
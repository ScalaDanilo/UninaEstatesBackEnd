package com.dieti.backend.service

import com.dieti.backend.dto.RichiestaDTO
import com.dieti.backend.repository.AgenteRepository
import com.dieti.backend.repository.ImmobileRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class GestioneImmobiliService(
    private val immobileRepository: ImmobileRepository,
    private val agenteRepository: AgenteRepository,
    private val notificaService: NotificaService, // Service DB
    private val firebaseService: FirebaseNotificationService // Service Push
) {

    @Transactional(readOnly = true)
    fun getImmobiliDaApprovare(managerIdStr: String): List<RichiestaDTO> {
        val managerId = UUID.fromString(managerIdStr)

        val manager = agenteRepository.findById(managerId)
            .orElseThrow { EntityNotFoundException("Manager non trovato") }

        val agenziaId = manager.agenzia?.uuid
            ?: throw IllegalStateException("Il manager non appartiene a nessuna agenzia")

        val immobiliDaApprovare = immobileRepository.findRichiestePendentiPerAgenzia(agenziaId)

        return immobiliDaApprovare.map { immobile ->
            val firstImage = immobile.immagini.firstOrNull()?.id?.let { "/api/immobili/immagini/$it/raw" }

            RichiestaDTO(
                id = immobile.uuid.toString(),
                titolo = immobile.categoria?.uppercase() ?: "IMMOBILE",
                descrizione = "${immobile.localita ?: "N/D"} - ${immobile.indirizzo ?: "N/D"}",
                data = immobile.annoCostruzione?.format(DateTimeFormatter.ISO_DATE) ?: "Recente",
                stato = "DA APPROVARE",
                immagineUrl = firstImage
            )
        }
    }

    @Transactional
    fun accettaImmobile(immobileIdStr: String, managerIdStr: String) {
        val immobileId = UUID.fromString(immobileIdStr)
        val managerId = UUID.fromString(managerIdStr)

        val immobile = immobileRepository.findById(immobileId)
            .orElseThrow { EntityNotFoundException("Immobile non trovato") }

        val manager = agenteRepository.findById(managerId)
            .orElseThrow { EntityNotFoundException("Manager non trovato") }

        // Assegna agente
        immobile.agente = manager
        immobileRepository.save(immobile)

        // --- INVIO NOTIFICHE ---

        // 1. Al Proprietario (Push + DB)
        val titoloOwner = "Immobile Pubblicato! 🏠"
        val corpoOwner = "Il tuo immobile in ${immobile.localita} è stato accettato dall'agente ${manager.nome}."

        notificaService.inviaNotifica(immobile.proprietario, titoloOwner, corpoOwner, "SISTEMA")
        firebaseService.sendNotificationToUser(immobile.proprietario, titoloOwner, corpoOwner) { it.notifPubblicazione }

        // 2. Broadcast (Nuovi Immobili in zona)
        if (!immobile.localita.isNullOrBlank()) {
            val indirizzoCompleto = if(!immobile.indirizzo.isNullOrBlank())
                "${immobile.localita}, ${immobile.indirizzo}"
            else immobile.localita!!

            firebaseService.notifyUsersForNewProperty(immobile.localita!!, indirizzoCompleto)
        }
    }

    @Transactional
    fun rifiutaImmobile(immobileIdStr: String) {
        val immobileId = UUID.fromString(immobileIdStr)
        val immobile = immobileRepository.findById(immobileId)
            .orElseThrow { EntityNotFoundException("Immobile non trovato") }

        // --- INVIO NOTIFICHE ---

        // 1. Al Proprietario (Push + DB)
        val titolo = "Richiesta Rifiutata ❌"
        val corpo = "Ci dispiace, la richiesta per ${immobile.localita} non è stata accettata dall'agenzia."

        notificaService.inviaNotifica(immobile.proprietario, titolo, corpo)
        firebaseService.sendNotificationToUser(immobile.proprietario, titolo, corpo) { it.notifPubblicazione }

        // Elimina
        immobileRepository.delete(immobile)
    }
}
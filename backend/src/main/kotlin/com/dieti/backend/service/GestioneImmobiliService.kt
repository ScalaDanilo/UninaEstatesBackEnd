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
    private val agenteRepository: AgenteRepository, // AGGIUNTO per recuperare l'agenzia del manager
    private val notificaService: NotificaService // Opzionale: per notificare l'utente (se lo usi)
) {

    @Transactional(readOnly = true)
    fun getImmobiliDaApprovare(managerIdStr: String): List<RichiestaDTO> {
        val managerId = UUID.fromString(managerIdStr)

        // 1. Recupero il manager (agente) per capire a quale agenzia appartiene
        val manager = agenteRepository.findById(managerId)
            .orElseThrow { EntityNotFoundException("Manager non trovato") }

        val agenziaId = manager.agenzia?.uuid
            ?: throw IllegalStateException("Il manager non appartiene a nessuna agenzia")

        // 2. Uso la query SQL ottimizzata nel repository invece di scaricare tutto il DB
        val immobiliDaApprovare = immobileRepository.findRichiestePendentiPerAgenzia(agenziaId)

        return immobiliDaApprovare.map { immobile ->
            // Recupero il path della prima immagine, se presente
            val firstImage = immobile.immagini.firstOrNull()?.id?.let { "/api/immagini/$it/raw" }

            RichiestaDTO(
                id = immobile.uuid.toString(),
                titolo = immobile.categoria?.uppercase() ?: "IMMOBILE",
                descrizione = "${immobile.localita ?: "N/D"} - ${immobile.indirizzo ?: "N/D"}",
                data = immobile.annoCostruzione?.format(DateTimeFormatter.ISO_DATE) ?: "Recente",
                stato = "DA APPROVARE",
                immagineUrl = firstImage // Passo l'immagine alla UI
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

        // FIX CRITICO: Assegniamo l'agente all'immobile.
        // NON sovrascriviamo il proprietario!
        immobile.agente = manager

        immobileRepository.save(immobile)
    }

    @Transactional
    fun rifiutaImmobile(immobileIdStr: String) {
        val immobileId = UUID.fromString(immobileIdStr)
        val immobile = immobileRepository.findById(immobileId)
            .orElseThrow { EntityNotFoundException("Immobile non trovato") }

        // Opzionale ma consigliato: avvisare l'utente prima di eliminare
        // notificaService.inviaNotifica(immobile.proprietario, "Richiesta Rifiutata", "Il tuo immobile in ${immobile.indirizzo} non è stato preso in carico.")

        // Il manager rifiuta la notifica: elimina definitivamente l'immobile dal DB
        immobileRepository.delete(immobile)
    }
}
package com.dieti.backend.service

import com.dieti.backend.dto.MessaggioTrattativaDTO
import com.dieti.backend.dto.StoriaTrattativaDTO
import com.dieti.backend.dto.TrattativaSummaryDTO
import com.dieti.backend.repository.OffertaRepository
import com.dieti.backend.repository.RispostaRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class TrattativaService(
    private val offertaRepository: OffertaRepository,
    private val rispostaRepository: RispostaRepository
) {

    @Transactional(readOnly = true)
    fun getTrattativeUtente(userIdStr: String): List<TrattativaSummaryDTO> {
        val userUuid = UUID.fromString(userIdStr)
        val offerte = offertaRepository.findAllByOfferenteUuid(userUuid)
        return mapOfferteToDTOAndSort(offerte)
    }

    @Transactional(readOnly = true)
    fun getTrattativeManager(agenteIdStr: String): List<TrattativaSummaryDTO> {
        val agenteUuid = UUID.fromString(agenteIdStr)
        val offerte = offertaRepository.findAllByAgente(agenteUuid)
        return mapOfferteToDTOAndSort(offerte)
    }

    // Funzione helper che mappa E ORDINA la lista
    private fun mapOfferteToDTOAndSort(offerte: List<com.dieti.backend.entity.OffertaEntity>): List<TrattativaSummaryDTO> {
        val dtoList = offerte.map { off ->
            val risposte = rispostaRepository.findAllByOffertaUuidOrderByDataRispostaAsc(off.uuid!!)
            val ultimaRisposta = risposte.lastOrNull()

            val stato = ultimaRisposta?.tipo ?: "NUOVA PROPOSTA"
            val data = (ultimaRisposta?.dataRisposta ?: off.dataOfferta).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            val img = if (off.immobile.immagini.isNotEmpty()) {
                "/api/immobili/immagini/${off.immobile.immagini[0].id}/raw"
            } else null

            val titolo = off.immobile.categoria?.uppercase() ?: "IMMOBILE"
            val indirizzo = if (!off.immobile.indirizzo.isNullOrBlank()) {
                "${off.immobile.localita} - ${off.immobile.indirizzo}"
            } else {
                off.immobile.localita
            }

            val offerente = "${off.offerente.nome} ${off.offerente.cognome}".trim()

            TrattativaSummaryDTO(
                offertaId = off.uuid.toString(),
                immobileId = off.immobile.uuid.toString(),
                immobileTitolo = titolo,
                immobileIndirizzo = indirizzo,
                prezzoOfferto = off.prezzoOfferta,
                nomeOfferente = offerente,
                ultimoStato = stato,
                ultimaModifica = data,
                immagineUrl = img
            )
        }

        // --- LOGICA DI ORDINAMENTO ---
        return dtoList.sortedWith(
            compareBy<TrattativaSummaryDTO> {
                // 1. Criterio Primario: Trattative chiuse in fondo
                val isTerminated = it.ultimoStato == "ACCETTATA" || it.ultimoStato == "RIFIUTATA"
                isTerminated // false (Active) < true (Terminated), quindi Active appare prima
            }.thenByDescending {
                // 2. Criterio Secondario: Data più recente in alto
                try {
                    LocalDate.parse(it.ultimaModifica, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                } catch (e: Exception) {
                    LocalDate.MIN
                }
            }
        )
    }

    @Transactional(readOnly = true)
    fun getStoriaTrattativa(offertaIdStr: String, viewerIdStr: String): StoriaTrattativaDTO {
        val uuid = UUID.fromString(offertaIdStr)
        val offerta = offertaRepository.findById(uuid)
            .orElseThrow { EntityNotFoundException("Offerta non trovata") }

        val risposte = rispostaRepository.findAllByOffertaUuidOrderByDataRispostaAsc(uuid)
        val messaggi = mutableListOf<MessaggioTrattativaDTO>()

        messaggi.add(MessaggioTrattativaDTO(
            autoreNome = if (offerta.offerente.uuid.toString() == viewerIdStr) "Tu" else offerta.offerente.nome,
            isMe = offerta.offerente.uuid.toString() == viewerIdStr,
            testo = offerta.corpo ?: "Offerta iniziale inviata",
            prezzo = offerta.prezzoOfferta,
            tipo = "OFFERTA_INIZIALE",
            data = offerta.dataOfferta.format(DateTimeFormatter.ISO_LOCAL_DATE)
        ))

        messaggi.addAll(risposte.map { r ->
            val isMessaggioDaLatoManager = r.mittente.uuid == offerta.venditore.uuid
            val isViewerAgente = offerta.immobile.agente?.uuid.toString() == viewerIdStr

            val isMe = if (isViewerAgente) isMessaggioDaLatoManager else (r.mittente.uuid.toString() == viewerIdStr)
            val displayNome = if (isMessaggioDaLatoManager) "Agente" else r.mittente.nome

            MessaggioTrattativaDTO(
                autoreNome = if (isMe) "Tu" else displayNome,
                isMe = isMe,
                testo = r.corpo ?: "",
                prezzo = r.prezzoProposto,
                tipo = r.tipo,
                data = r.dataRisposta.format(DateTimeFormatter.ISO_LOCAL_DATE)
            )
        })

        val ultimoMessaggio = risposte.lastOrNull()
        val isViewerAgente = offerta.immobile.agente?.uuid.toString() == viewerIdStr
        var canReply = false

        if (isViewerAgente) {
            if (ultimoMessaggio == null) {
                canReply = true
            } else {
                val isUltimoDaUtente = ultimoMessaggio.mittente.uuid == offerta.offerente.uuid
                if (isUltimoDaUtente && ultimoMessaggio.tipo == "CONTROPROPOSTA") {
                    canReply = true
                }
            }
        } else {
            val isUltimoDaAgente = ultimoMessaggio?.mittente?.uuid == offerta.venditore.uuid
            if (isUltimoDaAgente && ultimoMessaggio?.tipo == "CONTROPROPOSTA") {
                canReply = true
            }
        }

        if (ultimoMessaggio?.tipo == "ACCETTATA" || ultimoMessaggio?.tipo == "RIFIUTATA") {
            canReply = false
        }

        return StoriaTrattativaDTO(
            offertaId = offertaIdStr,
            prezzoIniziale = offerta.prezzoOfferta,
            immobileTitolo = "${offerta.immobile.localita} - ${offerta.immobile.indirizzo}",
            cronologia = messaggi,
            canUserReply = canReply
        )
    }
}
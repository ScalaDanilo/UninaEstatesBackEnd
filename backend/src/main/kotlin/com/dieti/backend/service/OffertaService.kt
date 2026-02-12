package com.dieti.backend.service

import com.dieti.backend.dto.OffertaRequest
import com.dieti.backend.dto.OffertaRicevutaDTO
import com.dieti.backend.entity.OffertaEntity
import com.dieti.backend.repository.ImmobileRepository
import com.dieti.backend.repository.OffertaRepository
import com.dieti.backend.repository.UtenteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class OffertaService(
    private val offertaRepository: OffertaRepository,
    private val utenteRepository: UtenteRepository,
    private val immobileRepository: ImmobileRepository
) {

    @Transactional
    fun creaOfferta(request: OffertaRequest): OffertaEntity {
        val offerente = utenteRepository.findById(UUID.fromString(request.utenteId))
            .orElseThrow { RuntimeException("Utente offerente non trovato") }

        val immobile = immobileRepository.findById(UUID.fromString(request.immobileId))
            .orElseThrow { RuntimeException("Immobile non trovato") }

        // Controllo duplicati
        if (offertaRepository.existsByOfferenteUuidAndImmobileUuid(offerente.uuid!!, immobile.uuid!!)) {
            throw IllegalArgumentException("Hai già inviato un'offerta per questo immobile.")
        }

        // Il venditore è il proprietario dell'immobile (Utente), ma l'agente gestisce la pratica
        val venditore = immobile.proprietario

        if (offerente.uuid == venditore.uuid) {
            throw RuntimeException("Non puoi fare un'offerta sul tuo stesso immobile")
        }

        val nuovaOfferta = OffertaEntity(
            offerente = offerente,
            venditore = venditore,
            immobile = immobile,
            prezzoOfferta = request.importo,
            corpo = request.corpo
        )

        return offertaRepository.save(nuovaOfferta)
    }

    @Transactional(readOnly = true)
    fun getOffertePendentiPerAgente(agenteId: String): List<OffertaRicevutaDTO> {
        val uuid = try {
            UUID.fromString(agenteId)
        } catch (e: Exception) {
            throw RuntimeException("ID Agente non valido")
        }

        // Recupera le offerte assegnate a questo agente
        val offerte = offertaRepository.findOffertePendenti(uuid)

        return offerte.map { offerta ->
            // Estrazione sicura dell'immagine
            val imgUrl = if (offerta.immobile.immagini.isNotEmpty()) {
                "/api/immobili/immagini/${offerta.immobile.immagini[0].id}"
            } else {
                null
            }

            val titolo = if (!offerta.immobile.indirizzo.isNullOrBlank()) {
                "${offerta.immobile.localita ?: ""} - ${offerta.immobile.indirizzo}"
            } else {
                offerta.immobile.localita ?: "Immobile senza indirizzo"
            }

            OffertaRicevutaDTO(
                id = offerta.uuid.toString(),
                nomeOfferente = offerta.offerente.nome,
                cognomeOfferente = offerta.offerente.cognome,
                prezzoOfferto = offerta.prezzoOfferta,
                immobileId = offerta.immobile.uuid.toString(),
                immobileTitolo = titolo,
                immobilePrezzoBase = offerta.immobile.prezzo ?: 0,
                immagineUrl = imgUrl,
                dataOfferta = offerta.dataOfferta
            )
        }
    }
}
package com.dieti.backend.controller

import com.dieti.backend.dto.*
import com.dieti.backend.entity.ImmagineEntity
import com.dieti.backend.entity.ImmobileEntity
import com.dieti.backend.repository.ImmobileRepository
import com.dieti.backend.repository.UtenteRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/immobili")
@CrossOrigin(origins = ["*"])
class ImmobileController(
    private val immobileRepository: ImmobileRepository,
    private val utenteRepository: UtenteRepository
) {

    // Lista Immobili (Home)
    @GetMapping
    fun getAllImmobili(
        @RequestParam(required = false) localita: String?,
        @RequestParam(required = false) tipologia: String?,
        @RequestParam(required = false) prezzoMax: Int?
    ): List<ImmobileDTO> {
        val immobili = immobileRepository.findAll()

        return immobili.filter { imm ->
            (localita == null || imm.localita?.contains(localita, ignoreCase = true) == true) &&
                    (tipologia == null || imm.tipologia?.equals(tipologia, ignoreCase = true) == true) &&
                    (prezzoMax == null || (imm.prezzo ?: 0) <= prezzoMax)
        }.map { toSummaryDTO(it) }
    }

    // Dettaglio Immobile
    @GetMapping("/{id}")
    fun getImmobileDetail(@PathVariable id: UUID): ResponseEntity<ImmobileDetailDTO> {
        val immobile = immobileRepository.findById(id)
        return if (immobile.isPresent) {
            ResponseEntity.ok(toDetailDTO(immobile.get()))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // Crea Immobile
    @PostMapping
    fun createImmobile(@RequestBody request: ImmobileCreateRequest): ResponseEntity<Any> {
        val proprietario = utenteRepository.findById(request.proprietarioId)
        if (proprietario.isEmpty) return ResponseEntity.badRequest().body("Utente non trovato")

        val newImmobile = ImmobileEntity(
            proprietario = proprietario.get(),
            tipoVendita = request.tipoVendita,
            categoria = request.categoria,
            tipologia = request.tipologia,
            localita = request.localita,
            mq = request.mq,
            piano = request.piano,
            ascensore = request.ascensore,
            dettagli = request.dettagli,
            arredamento = request.arredamento,
            climatizzazione = request.climatizzazione,
            esposizione = request.esposizione,
            tipoProprieta = request.tipoProprieta,
            statoProprieta = request.statoProprieta,
            prezzo = request.prezzo,
            speseCondominiali = request.speseCondominiali,
            descrizione = request.descrizione,
            disponibilita = true
        )

        val saved = immobileRepository.save(newImmobile)
        return ResponseEntity.ok(mapOf("uuid" to saved.uuid))
    }

    // Upload Immagine
    @PostMapping("/{id}/immagini", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(
        @PathVariable id: UUID,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        val immobileOpt = immobileRepository.findById(id)
        if (immobileOpt.isEmpty) return ResponseEntity.notFound().build()

        val immobile = immobileOpt.get()
        val immagineEntity = ImmagineEntity(
            nome = file.originalFilename,
            formato = file.contentType,
            immagine = file.bytes,
            immobile = immobile
        )

        immobile.immagini.add(immagineEntity)
        immobileRepository.save(immobile)

        return ResponseEntity.ok("Immagine caricata")
    }

    // Cancella Immobile
    @DeleteMapping("/{id}")
    fun deleteImmobile(@PathVariable id: UUID): ResponseEntity<String> {
        if (immobileRepository.existsById(id)) {
            immobileRepository.deleteById(id)
            return ResponseEntity.ok("Eliminato")
        }
        return ResponseEntity.notFound().build()
    }

    // --- MAPPERS ---
    private fun toSummaryDTO(entity: ImmobileEntity): ImmobileDTO {
        val coverId = if (entity.immagini.isNotEmpty()) entity.immagini[0].id else null
        return ImmobileDTO(
            id = entity.uuid!!,
            titolo = "${entity.tipologia} a ${entity.localita}",
            prezzo = entity.prezzo ?: 0,
            tipologia = entity.tipologia,
            localita = entity.localita,
            mq = entity.mq,
            descrizione = entity.descrizione,
            coverImageId = coverId,
            isVendita = entity.tipoVendita,
            proprietarioId = entity.proprietario.uuid!!
        )
    }

    private fun toDetailDTO(entity: ImmobileEntity): ImmobileDetailDTO {
        return ImmobileDetailDTO(
            id = entity.uuid!!,
            proprietarioNome = "${entity.proprietario.nome} ${entity.proprietario.cognome}",
            proprietarioId = entity.proprietario.uuid!!,
            tipoVendita = entity.tipoVendita,
            categoria = entity.categoria,
            tipologia = entity.tipologia,
            localita = entity.localita,
            mq = entity.mq,
            piano = entity.piano,
            ascensore = entity.ascensore,
            dettagli = entity.dettagli,
            arredamento = entity.arredamento,
            climatizzazione = entity.climatizzazione,
            esposizione = entity.esposizione,
            tipoProprieta = entity.tipoProprieta,
            statoProprieta = entity.statoProprieta,
            annoCostruzione = entity.annoCostruzione,
            prezzo = entity.prezzo,
            speseCondominiali = entity.speseCondominiali,
            disponibilita = entity.disponibilita,
            descrizione = entity.descrizione,
            immaginiIds = entity.immagini.mapNotNull { it.id }
        )
    }
}
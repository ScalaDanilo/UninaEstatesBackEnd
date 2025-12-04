package com.dieti.backend.controller

import com.dieti.backend.dto.ImmobileCreateRequest
import com.dieti.backend.dto.ImmobileDTO
import com.dieti.backend.dto.ImmobileDetailDTO
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
@CrossOrigin(origins = ["*"]) // Abilita richieste da Android/Web
class ImmobileController(
    private val immobileRepository: ImmobileRepository,
    private val utenteRepository: UtenteRepository
) {

    // 1. Lista Immobili (Home Screen) con filtri opzionali
    @GetMapping
    fun getAllImmobili(
        @RequestParam(required = false) localita: String?,
        @RequestParam(required = false) tipologia: String?,
        @RequestParam(required = false) prezzoMax: Int?
    ): List<ImmobileDTO> {
        // Nota: Per filtri complessi, in produzione si usa CriteriaBuilder o QueryDSL.
        // Qui facciamo un filtro in memoria per semplicità, o dovresti aggiungere metodi al Repository.
        val immobili = immobileRepository.findAll()

        return immobili.filter { imm ->
            (localita == null || imm.localita?.contains(localita, ignoreCase = true) == true) &&
            (tipologia == null || imm.tipologia?.equals(tipologia, ignoreCase = true) == true) &&
            (prezzoMax == null || (imm.prezzo ?: 0) <= prezzoMax)
        }.map { toSummaryDTO(it) }
    }

    // 2. Dettaglio Immobile
    @GetMapping("/{id}")
    fun getImmobileDetail(@PathVariable id: UUID): ResponseEntity<ImmobileDetailDTO> {
        val immobile = immobileRepository.findById(id)
        return if (immobile.isPresent) {
            ResponseEntity.ok(toDetailDTO(immobile.get()))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // 3. Crea Nuovo Immobile (PropertySellScreen)
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
        return ResponseEntity.ok(mapOf("uuid" to saved.uuid)) // Ritorna l'ID per caricare poi le foto
    }

    // 4. Upload Immagine (Step successivo alla creazione)
    @PostMapping("/{id}/immagini", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(
        @PathVariable id: UUID,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        val immobileOpt = immobileRepository.findById(id)
        if (immobileOpt.isEmpty) return ResponseEntity.notFound().build()

        val immobile = immobileOpt.get()

        // Conversione Multipart -> Entity
        val immagineEntity = ImmagineEntity(
            nome = file.originalFilename,
            formato = file.contentType,
            immagine = file.bytes, // Qui avviene la magia del BYTEA
            immobile = immobile
        )

        immobile.immagini.add(immagineEntity)
        immobileRepository.save(immobile)

        return ResponseEntity.ok("Immagine caricata con successo")
    }

    // 5. Scarica Immagine (Per visualizzarla nell'App)
    // URL sarà: /api/immobili/immagini/{idImmagine}
    @GetMapping("/immagini/{idImmagine}")
    fun getImage(@PathVariable idImmagine: Int): ResponseEntity<ByteArray> {
        // Nota: Idealmente dovresti avere un ImmagineRepository, ma possiamo recuperarlo navigando l'immobile
        // Per performance, aggiungi: interface ImmagineRepository : JpaRepository<ImmagineEntity, Int>
        // Qui simulo il recupero diretto se avessi il repository iniettato,
        // ALTRIMENTI devi aggiungere ImmagineRepository al costruttore del controller.

        // Esempio logico supponendo tu aggiunga ImmagineRepository:
        // val img = immagineRepository.findById(idImmagine)
        // return ResponseEntity.ok().contentType(MediaType.parseMediaType(img.formato)).body(img.immagine)

        return ResponseEntity.notFound().build() // Placeholder: Aggiungi ImmagineRepository!
    }

    // --- MAPPERS ---

    private fun toSummaryDTO(entity: ImmobileEntity): ImmobileDTO {
        // Prende l'ID della prima immagine se esiste per la cover
        val coverId = if (entity.immagini.isNotEmpty()) entity.immagini[0].id else null
        return ImmobileDTO(
            id = entity.uuid!!,
            titolo = "${entity.tipologia} in ${entity.localita}",
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
    // --- AGGIUNGI QUESTI METODI DENTRO ImmobileController ---

    // 6. Modifica Immobile (PUT)
    @PutMapping("/{id}")
    fun updateImmobile(
        @PathVariable id: UUID,
        @RequestBody request: ImmobileCreateRequest
    ): ResponseEntity<String> {
        val immobileOpt = immobileRepository.findById(id)
        if (immobileOpt.isEmpty) return ResponseEntity.notFound().build()

        val immobile = immobileOpt.get()

        // Aggiorniamo i campi (puoi usare un mapper o farlo a mano)
        // Nota: Poiché ImmobileEntity è una data class con val, in JPA spesso si usano var o si usa copy + save.
        // Se hai definito i campi come 'val' nell'Entity, devi cambiarli in 'var' per modificarli così,
        // oppure creare un nuovo oggetto copiando l'ID.
        // Assumiamo che tu abbia cambiato i campi in 'var' nell'Entity come consigliato per JPA.

        val updated = immobile.copy(
            tipoVendita = request.tipoVendita,
            categoria = request.categoria,
            tipologia = request.tipologia,
            localita = request.localita,
            mq = request.mq,
            prezzo = request.prezzo,
            descrizione = request.descrizione,
            // ... altri campi
        )
        // Hack per JPA: copy() crea un nuovo oggetto, dobbiamo assicurarci che l'ID sia lo stesso
        // Se Hibernate si lamenta "detached entity", meglio settare i campi uno a uno su 'immobile' (se sono var).

        immobileRepository.save(updated)
        return ResponseEntity.ok("Immobile aggiornato")
    }

    // 7. Cancella Immobile (DELETE)
    @DeleteMapping("/{id}")
    fun deleteImmobile(@PathVariable id: UUID): ResponseEntity<String> {
        if (immobileRepository.existsById(id)) {
            immobileRepository.deleteById(id)
            return ResponseEntity.ok("Immobile eliminato")
        }
        return ResponseEntity.notFound().build()
    }
}
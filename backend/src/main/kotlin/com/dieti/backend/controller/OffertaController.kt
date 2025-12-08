package com.dieti.backend.controller

import com.dieti.backend.dto.OffertaDTO
import com.dieti.backend.dto.OffertaRequest
import com.dieti.backend.entity.OffertaEntity
import com.dieti.backend.repository.ImmobileRepository
import com.dieti.backend.repository.OffertaRepository
import com.dieti.backend.repository.UtenteRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/offerte")
@CrossOrigin(origins = ["*"])
class OffertaController(
    private val offertaRepository: OffertaRepository,
    private val utenteRepository: UtenteRepository,
    private val immobileRepository: ImmobileRepository
) {

    @PostMapping("/fai-offerta")
    fun creaOfferta(@RequestBody req: OffertaRequest): ResponseEntity<String> {
        val offerente = utenteRepository.findById(req.utenteId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Utente non trovato")

        val immobile = immobileRepository.findById(req.immobileId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Immobile non trovato")

        val venditore = immobile.proprietario

        val nuovaOfferta = OffertaEntity(
            offerente = offerente,
            venditore = venditore,
            immobile = immobile,
            prezzoOfferta = req.importo,
            corpo = req.corpo
        )

        offertaRepository.save(nuovaOfferta)
        return ResponseEntity.ok("Offerta inviata al proprietario")
    }

    @GetMapping("/per-immobile/{immobileId}")
    fun getOffertePerImmobile(@PathVariable immobileId: UUID): List<OffertaDTO> {
        val offerte = offertaRepository.findByImmobileUuid(immobileId)

        return offerte.map {
            OffertaDTO(
                id = it.uuid!!,
                immobileTitolo = it.immobile.tipologia ?: "Immobile",
                importo = it.prezzoOfferta,
                data = it.dataOfferta.toString()
            )
        }
    }
}
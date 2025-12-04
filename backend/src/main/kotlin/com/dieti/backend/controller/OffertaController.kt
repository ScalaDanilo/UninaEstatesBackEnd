package com.dieti.backend.controller

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
class OffertaController(
    val offertaRepo: OffertaRepository,
    val utenteRepo: UtenteRepository,
    val immobileRepo: ImmobileRepository
) {

    @PostMapping("/fai-offerta")
    fun creaOfferta(@RequestBody req: OffertaRequest): ResponseEntity<String> {
        val offerente = utenteRepo.findById(UUID.fromString(req.offerenteId)).orElseThrow()
        val immobile = immobileRepo.findById(UUID.fromString(req.immobileId)).orElseThrow()
        
        // Il venditore è il proprietario dell'immobile
        val venditore = immobile.proprietario

        val nuovaOfferta = OffertaEntity(
            offerente = offerente,
            venditore = venditore,
            immobile = immobile,
            prezzoOfferta = req.prezzo,
            corpo = req.corpo
        )

        offertaRepo.save(nuovaOfferta)
        
        // TODO: RabbitMQ -> Invia notifica al venditore "Hai una nuova offerta!"
        
        return ResponseEntity.ok("Offerta inviata al proprietario!")
    }

    @GetMapping("/per-immobile/{immobileId}")
    fun getOffertePerImmobile(@PathVariable immobileId: String): List<Map<String, Any>> {
        val offerte = offertaRepo.findAll()
            .filter { it.immobile.uuid.toString() == immobileId }

        return offerte.map { 
            mapOf(
                "id" to (it.uuid.toString()),
                "offerente" to "${it.offerente.nome} ${it.offerente.cognome}",
                "prezzo" to it.prezzoOfferta,
                "data" to it.dataOfferta.toString()
            )
        }
    }
}
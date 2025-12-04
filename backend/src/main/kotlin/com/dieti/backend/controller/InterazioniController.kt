package com.dieti.backend.controller

import com.dieti.backend.dto.NotificaDTO
import com.dieti.backend.dto.OffertaDTO
import com.dieti.backend.dto.OffertaRequest
import com.dieti.backend.entity.NotificaEntity
import com.dieti.backend.entity.OffertaEntity
import com.dieti.backend.repository.ImmobileRepository
import com.dieti.backend.repository.NotificaRepository
import com.dieti.backend.repository.OffertaRepository
import com.dieti.backend.repository.UtenteRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

// --- OFFERTA CONTROLLER ---
@RestController
@RequestMapping("/api/offerte")
@CrossOrigin(origins = ["*"])
class OffertaController(
    private val offertaRepository: OffertaRepository,
    private val utenteRepository: UtenteRepository,
    private val immobileRepository: ImmobileRepository
) {
    @PostMapping
    fun makeOffer(@RequestBody req: OffertaRequest): ResponseEntity<String> {
        val utente = utenteRepository.findById(req.utenteId).orElse(null) ?: return ResponseEntity.notFound().build()
        val immobile = immobileRepository.findById(req.immobileId).orElse(null) ?: return ResponseEntity.notFound().build()

        val offerta = OffertaEntity(
            utente = utente,
            immobile = immobile,
            importo = req.importo,
            stato = "PENDING"
        )
        offertaRepository.save(offerta)
        return ResponseEntity.ok("Offerta inviata")
    }

    @GetMapping("/utente/{userId}")
    fun getOffers(@PathVariable userId: UUID): List<OffertaDTO> {
        // Usa query method nel repo in produzione: findByUtenteUuid
        val all = offertaRepository.findAll()
        return all.filter { it.utente?.uuid == userId }.map {
            OffertaDTO(it.id ?: 0, it.immobile?.tipologia ?: "", it.importo ?: 0, it.stato ?: "")
        }
    }
}

// --- NOTIFICA CONTROLLER ---
@RestController
@RequestMapping("/api/notifiche")
@CrossOrigin(origins = ["*"])
class NotificaController(
    private val notificaRepository: NotificaRepository,
    private val utenteRepository: UtenteRepository
) {
    @GetMapping("/utente/{userId}")
    fun getNotifiche(@PathVariable userId: UUID): List<NotificaDTO> {
        val all = notificaRepository.findAll()
        // Filtra in memoria (in produzione usa findByDestinatarioUuid)
        return all.filter { it.destinatario?.uuid == userId }.map {
            NotificaDTO(it.id ?: 0, it.messaggio ?: "", it.data ?: LocalDate.now(), it.letta)
        }
    }

    @PutMapping("/{id}/leggi")
    fun markAsRead(@PathVariable id: Int): ResponseEntity<String> {
        val notifica = notificaRepository.findById(id)
        if (notifica.isPresent) {
            val n = notifica.get()
            // n.letta = true // Se 'letta' è var
            notificaRepository.save(n)
            return ResponseEntity.ok("Letta")
        }
        return ResponseEntity.notFound().build()
    }
}
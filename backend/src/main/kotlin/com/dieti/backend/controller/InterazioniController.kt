package com.dieti.backend.controller

import com.dieti.backend.dto.NotificaDTO
import com.dieti.backend.repository.NotificaRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/notifiche")
@CrossOrigin(origins = ["*"])
class NotificaController(
    private val notificaRepository: NotificaRepository
) {

    @GetMapping("/utente/{userId}")
    fun getNotifiche(@PathVariable userId: UUID): List<NotificaDTO> {
        val all = notificaRepository.findByUtenteUuidOrderByDataCreazioneDesc(userId)

        return all.map {
            NotificaDTO(
                id = it.uuid!!,
                titolo = it.titolo,
                corpo = it.corpo,
                data = it.dataCreazione.toString(),
                letto = it.letto
            )
        }
    }

    @PutMapping("/{id}/leggi")
    fun markAsRead(@PathVariable id: UUID): ResponseEntity<String> {
        val notificaOpt = notificaRepository.findById(id)
        if (notificaOpt.isPresent) {
            val notifica = notificaOpt.get()
            // Creiamo una copia aggiornata dell'entità
            val updated = notifica.copy(letto = true)
            notificaRepository.save(updated)
            return ResponseEntity.ok("Notifica segnata come letta")
        }
        return ResponseEntity.notFound().build()
    }
}
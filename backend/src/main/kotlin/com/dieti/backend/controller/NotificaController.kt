package com.dieti.backend.controller

import com.dieti.backend.dto.NotificaDTO
import com.dieti.backend.service.NotificaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifiche")
@CrossOrigin(origins = ["*"])
class NotificaController(
    private val notificaService: NotificaService
) {

    @GetMapping("/utente/{userId}")
    fun getNotificheUtente(@PathVariable userId: String): ResponseEntity<List<NotificaDTO>> {
        return try {
            val notifiche = notificaService.getNotificheUtente(userId)
            ResponseEntity.ok(notifiche)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.ok(emptyList()) // Evita crash ritornando lista vuota
        }
    }
}
package com.dieti.backend.controller

import com.dieti.backend.service.NotificaService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifiche")
@CrossOrigin(origins = ["*"])
class NotificaController(
    private val notificaService: NotificaService
) {


}
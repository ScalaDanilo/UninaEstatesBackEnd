package com.dieti.backend.controller

import com.dieti.backend.repository.ImmobileRepository
import com.dieti.backend.repository.UtenteRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/utenti")
@CrossOrigin(origins = ["*"])
class UtenteController(
    private val utenteRepository: UtenteRepository,
    private val immobileRepository: ImmobileRepository
) {

}
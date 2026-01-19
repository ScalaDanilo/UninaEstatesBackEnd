package com.dieti.backend.controller

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

}
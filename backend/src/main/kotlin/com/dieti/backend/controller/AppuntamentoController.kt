package com.dieti.backend.controller

import com.dieti.backend.repository.AgenteRepository
import com.dieti.backend.repository.ImmobileRepository
import com.dieti.backend.service.AppuntamentoService
import com.dieti.backend.service.UtenteService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/appuntamenti")
@CrossOrigin(origins = ["*"])
class AppuntamentoController(
    private val appuntamentoService: AppuntamentoService,
    private val utenteService: UtenteService,
    private val immobileRepository: ImmobileRepository,
    private val agenteRepository: AgenteRepository
) {

}
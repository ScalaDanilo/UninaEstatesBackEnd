package com.dieti.backend.controller

import com.dieti.backend.service.AgenteService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/agenti")
@CrossOrigin(origins = ["*"])
class AgenteController(
    private val agenteService: AgenteService
) {

}
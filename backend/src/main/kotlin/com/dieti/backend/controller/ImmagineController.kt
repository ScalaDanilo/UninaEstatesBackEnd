package com.dieti.backend.controller

import com.dieti.backend.repository.ImmagineRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/immagini")
@CrossOrigin(origins = ["*"])
class ImmagineController(
    private val immagineRepository: ImmagineRepository
) {

}
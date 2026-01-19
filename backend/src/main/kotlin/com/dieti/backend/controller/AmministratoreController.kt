package com.dieti.backend.controller

import com.dieti.backend.service.AmministratoreService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/amministratori")
@CrossOrigin(origins = ["*"])
class AmministratoreController (
    private val amministratoreService: AmministratoreService
){
}
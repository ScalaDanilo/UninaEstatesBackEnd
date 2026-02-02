package com.dieti.backend.controller

import com.dieti.backend.service.ImmagineService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/immagini")
class ImmagineController(private val immagineService: ImmagineService) {

    @GetMapping("/{id}/raw")
    fun getImmagineRaw(@PathVariable id: Int): ResponseEntity<ByteArray> {
        val immagine = immagineService.getImmagineContent(id)

        val headers = HttpHeaders()
        // Imposta il content type corretto (jpeg, png, etc.) o defaulta a jpeg
        headers.contentType = MediaType.parseMediaType(immagine.formato ?: "image/jpeg")
        headers.contentLength = immagine.immagine?.size?.toLong() ?: 0

        return ResponseEntity(immagine.immagine, headers, HttpStatus.OK)
    }
}
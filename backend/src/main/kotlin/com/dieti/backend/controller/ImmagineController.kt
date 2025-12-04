package com.dieti.backend.controller

import com.dieti.backend.repository.ImmagineRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// Assicurati di creare questo repository:
// interface ImmagineRepository : JpaRepository<ImmagineEntity, Int>

@RestController
@RequestMapping("/api/immagini")
@CrossOrigin(origins = ["*"])
class ImmagineController(
    private val immagineRepository: ImmagineRepository
) {

    @GetMapping("/{id}")
    fun getImmagine(@PathVariable id: Int): ResponseEntity<ByteArray> {
        val immagineOpt = immagineRepository.findById(id)
        
        if (immagineOpt.isPresent) {
            val img = immagineOpt.get()
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(img.formato ?: "image/jpeg"))
                .body(img.immagine)
        }
        return ResponseEntity.notFound().build()
    }
}
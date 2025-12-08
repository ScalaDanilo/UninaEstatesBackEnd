package com.dieti.backend.controller

import com.dieti.backend.dto.ImmobileDTO
import com.dieti.backend.dto.UserProfileDTO
import com.dieti.backend.dto.UserUpdateRequest
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

    @GetMapping("/{id}")
    fun getProfile(@PathVariable id: UUID): ResponseEntity<UserProfileDTO> {
        val user = utenteRepository.findById(id)
        return if (user.isPresent) {
            val u = user.get()
            ResponseEntity.ok(UserProfileDTO(u.uuid!!, u.nome, u.cognome, u.email, u.telefono))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}")
    fun updateProfile(@PathVariable id: UUID, @RequestBody request: UserUpdateRequest): ResponseEntity<Any> {
        val userOpt = utenteRepository.findById(id)
        if (userOpt.isEmpty) return ResponseEntity.notFound().build()

        val user = userOpt.get()
        val updatedUser = user.copy(
            telefono = request.telefono ?: user.telefono,
            password = request.password ?: user.password
        )

        utenteRepository.save(updatedUser)
        return ResponseEntity.ok("Profilo aggiornato")
    }

    @PostMapping("/{userId}/preferiti/{immobileId}")
    fun addPreferito(@PathVariable userId: UUID, @PathVariable immobileId: UUID): ResponseEntity<String> {
        val user = utenteRepository.findById(userId).orElse(null) ?: return ResponseEntity.notFound().build()
        val immobile = immobileRepository.findById(immobileId).orElse(null) ?: return ResponseEntity.notFound().build()

        user.preferiti.add(immobile)
        utenteRepository.save(user)
        return ResponseEntity.ok("Aggiunto ai preferiti")
    }

    @DeleteMapping("/{userId}/preferiti/{immobileId}")
    fun removePreferito(@PathVariable userId: UUID, @PathVariable immobileId: UUID): ResponseEntity<String> {
        val user = utenteRepository.findById(userId).orElse(null) ?: return ResponseEntity.notFound().build()

        user.preferiti.removeIf { it.uuid == immobileId }
        utenteRepository.save(user)
        return ResponseEntity.ok("Rimosso dai preferiti")
    }

    @GetMapping("/{userId}/preferiti")
    fun getPreferiti(@PathVariable userId: UUID): ResponseEntity<List<ImmobileDTO>> {
        val user = utenteRepository.findById(userId).orElse(null) ?: return ResponseEntity.notFound().build()

        val dtos = user.preferiti.map { entity ->
            val coverId = if (entity.immagini.isNotEmpty()) entity.immagini[0].id else null
            ImmobileDTO(
                id = entity.uuid!!,
                titolo = "${entity.tipologia} a ${entity.localita}",
                prezzo = entity.prezzo ?: 0,
                tipologia = entity.tipologia,
                localita = entity.localita,
                mq = entity.mq,
                descrizione = entity.descrizione,
                coverImageId = coverId,
                isVendita = entity.tipoVendita,
                proprietarioId = entity.proprietario.uuid!!
            )
        }
        return ResponseEntity.ok(dtos)
    }
}
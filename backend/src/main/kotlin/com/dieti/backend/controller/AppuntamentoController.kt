package com.dieti.backend.controller

import com.dieti.backend.dto.AppuntamentoDTO
import com.dieti.backend.dto.AppuntamentoRequest
import com.dieti.backend.entity.AppuntamentoEntity
import com.dieti.backend.repository.AppuntamentoRepository
import com.dieti.backend.repository.ImmobileRepository
import com.dieti.backend.repository.UtenteRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalTime
import java.util.UUID

@RestController
@RequestMapping("/api/appuntamenti")
@CrossOrigin(origins = ["*"])
class AppuntamentoController(
    private val appuntamentoRepository: AppuntamentoRepository,
    private val utenteRepository: UtenteRepository,
    private val immobileRepository: ImmobileRepository
) {

    // 1. Richiedi Appuntamento
    @PostMapping
    fun createAppuntamento(@RequestBody request: AppuntamentoRequest): ResponseEntity<String> {
        val utente = utenteRepository.findById(request.utenteId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Utente non trovato")
        val immobile = immobileRepository.findById(request.immobileId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Immobile non trovato")

        val appuntamento = AppuntamentoEntity(
            utente = utente,
            immobile = immobile,
            data = request.data,
            orario = LocalTime.parse(request.orario), // Assicurati che il formato sia HH:mm
            stato = "IN_ATTESA"
        )

        appuntamentoRepository.save(appuntamento)
        return ResponseEntity.ok("Richiesta inviata")
    }

    // 2. Lista appuntamenti per un utente (es. i miei appuntamenti)
    @GetMapping("/utente/{userId}")
    fun getAppuntamentiByUtente(@PathVariable userId: UUID): List<AppuntamentoDTO> {
        // Qui dovresti aggiungere un metodo findByUtenteUuid nel repository
        val list = appuntamentoRepository.findAll() // Placeholder, usa filtro o query method corretto

        return list.filter { it.utente?.uuid == userId }.map {
            AppuntamentoDTO(
                id = it.id ?: 0,
                immobileTitolo = "${it.immobile?.tipologia} - ${it.immobile?.localita}",
                data = it.data ?: java.time.LocalDate.now(),
                orario = it.orario.toString(),
                stato = it.stato ?: "N/A"
            )
        }
    }
}
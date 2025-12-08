package com.dieti.backend.controller

import com.dieti.backend.dto.AppuntamentoDTO
import com.dieti.backend.dto.AppuntamentoRequest
import com.dieti.backend.entity.AppuntamentoEntity
import com.dieti.backend.repository.AgenteRepository
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
    private val immobileRepository: ImmobileRepository,
    private val agenteRepository: AgenteRepository
) {

    @PostMapping
    fun createAppuntamento(@RequestBody request: AppuntamentoRequest): ResponseEntity<String> {
        val utente = utenteRepository.findById(request.utenteId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Utente non trovato")

        val immobile = immobileRepository.findById(request.immobileId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Immobile non trovato")

        val agente = agenteRepository.findById(request.agenteId).orElse(null)
            ?: return ResponseEntity.badRequest().body("Agente non trovato")

        val appuntamento = AppuntamentoEntity(
            utente = utente,
            immobile = immobile,
            agente = agente,
            data = request.data,
            ora = LocalTime.parse(request.orario),
            corpo = "Richiesta Appuntamento"
        )

        appuntamentoRepository.save(appuntamento)
        return ResponseEntity.ok("Appuntamento richiesto con successo")
    }

    @GetMapping("/utente/{userId}")
    fun getAppuntamentiByUtente(@PathVariable userId: UUID): List<AppuntamentoDTO> {
        val list = appuntamentoRepository.findByUtenteUuid(userId)

        return list.map {
            AppuntamentoDTO(
                id = it.uuid!!,
                immobileTitolo = "${it.immobile.tipologia} - ${it.immobile.localita}",
                data = it.data,
                orario = it.ora.toString()
            )
        }
    }
}
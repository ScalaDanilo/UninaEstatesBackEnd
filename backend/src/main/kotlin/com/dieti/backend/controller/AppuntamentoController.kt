package com.dieti.backend.controller

import com.dieti.backend.dto.AppuntamentoRequest
import com.dieti.backend.entity.AppuntamentoEntity
import com.dieti.backend.repository.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@RestController
@RequestMapping("/api/appuntamenti")
class AppuntamentoController(
    val appuntamentoRepo: AppuntamentoRepository,
    val utenteRepo: UtenteRepository,
    val immobileRepo: ImmobileRepository,
    val agenteRepo: AgenteRepository
) {

    @PostMapping("/crea")
    fun creaAppuntamento(@RequestBody req: AppuntamentoRequest): ResponseEntity<String> {
        val utente = utenteRepo.findById(UUID.fromString(req.userId)).orElseThrow()
        val immobile = immobileRepo.findById(UUID.fromString(req.immobileId)).orElseThrow()
        
        // PER SEMPLICITÀ: Assegniamo il primo agente disponibile nel DB 
        // (In un'app vera ci sarebbe una logica per scegliere l'agente dell'agenzia giusta)
        val agenti = agenteRepo.findAll()
        if (agenti.isEmpty()) return ResponseEntity.badRequest().body("Nessun agente disponibile")
        val agenteAssegnato = agenti[0] 

        val nuovoAppuntamento = AppuntamentoEntity(
            utente = utente,
            immobile = immobile,
            agente = agenteAssegnato,
            data = LocalDate.parse(req.data), // "2024-12-31"
            ora = LocalTime.parse(req.ora),   // "15:30"
            corpo = req.corpo
        )

        appuntamentoRepo.save(nuovoAppuntamento)
        
        // TODO: Qui potresti inviare un messaggio a RabbitMQ per notificare l'agente!
        
        return ResponseEntity.ok("Appuntamento richiesto con successo!")
    }

    @GetMapping("/miei/{userId}")
    fun getMieiAppuntamenti(@PathVariable userId: String): List<Map<String, String>> {
        // Qui restituisco una lista semplificata
        // In un caso reale useresti un DTO di risposta
        val appuntamenti = appuntamentoRepo.findAll() // Filtrali per utente se aggiungi il metodo nel Repo
            .filter { it.utente.uuid.toString() == userId }
            
        return appuntamenti.map { app ->
            mapOf(
                "data" to app.data.toString(),
                "ora" to app.ora.toString(),
                "immobile" to (app.immobile.titolo ?: "Immobile senza titolo"), // Campo titolo ipotetico
                "agente" to "${app.agente.nome} ${app.agente.cognome}"
            )
        }
    }
}
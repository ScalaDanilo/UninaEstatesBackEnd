package com.dieti.backend.controller

import com.dieti.backend.dto.ManagerDashboardStats
import com.dieti.backend.repository.ImmobileRepository
import com.dieti.backend.repository.OffertaRepository
import com.dieti.backend.repository.AgenteRepository // Modificato: Usiamo AgenteRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = ["*"])
class ManagerDashboardController(
    private val offertaRepository: OffertaRepository,
    private val immobileRepository: ImmobileRepository,
    private val agenteRepository: AgenteRepository // Modificato: il manager è un Agente
) {

    @GetMapping("/dashboard/{agenteId}")
    fun getDashboardStats(@PathVariable agenteId: String): ResponseEntity<ManagerDashboardStats> {
        return try {
            val uuid = UUID.fromString(agenteId)
            println("DEBUG DASHBOARD: Calcolo statistiche per manager/agente $uuid")

            // 1. Recupero il manager (che è un Agente) per conoscere la sua agenzia
            val manager = agenteRepository.findById(uuid).orElse(null)
            if (manager == null) {
                println("DEBUG DASHBOARD: Manager (Agente) non trovato nel DB!")
                return ResponseEntity.ok(ManagerDashboardStats(0, 0))
            }

            // 2. CONTA LE OFFERTE (PROPOSTE)
            // Logica: Tutte le offerte degli immobili dove agente_id = id_del_manager
            val allOfferte = offertaRepository.findAll()
            val countOfferte = allOfferte.count { offerta ->
                // Usiamo "agente" (la variabile che mappa agente_id in ImmobileEntity)
                offerta.immobile.agente?.uuid == uuid
            }

            // 3. CONTA LE NOTIFICHE (APPARTAMENTI DA APPROVARE)
            // Logica: Immobili della stessa agenzia del manager, con agente_id a NULL
            val allImmobili = immobileRepository.findAll()
            val countNotifiche = allImmobili.count { immobile ->

                // Controllo su agente_id
                val agenteRaw: Any? = immobile.agente
                val isSenzaAgente = agenteRaw == null

                // Controlla che l'agenzia dell'immobile corrisponda a quella del manager
                // Si assume che AgenteEntity abbia un campo 'agenzia'
                val isStessaAgenzia = immobile.agenzia?.uuid != null &&
                        immobile.agenzia?.uuid == manager.agenzia?.uuid

                isSenzaAgente && isStessaAgenzia
            }

            println("DEBUG DASHBOARD: Trovate $countOfferte offerte e $countNotifiche immobili da assegnare")

            ResponseEntity.ok(ManagerDashboardStats(countNotifiche, countOfferte))
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.badRequest().build()
        }
    }
}
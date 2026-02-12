package com.dieti.backend.service

import com.dieti.backend.dto.AgenteDTO
import com.dieti.backend.dto.CreateAgenteRequest
import com.dieti.backend.dto.ImmobileDTO
import com.dieti.backend.dto.toDTO
import com.dieti.backend.dto.toDto
import com.dieti.backend.entity.AgenteEntity
import com.dieti.backend.repository.AgenteRepository
import com.dieti.backend.repository.AgenziaRepository
import com.dieti.backend.repository.ImmobileRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AgenteService(
    private val agenteRepository: AgenteRepository,
    private val agenziaRepository: AgenziaRepository,
    private val immobileRepository: ImmobileRepository,
    private val notificaService: NotificaService,
    private val passwordEncoder: PasswordEncoder
) {

    // ... (creaAgente, getAgenteById, getAllAgenti, getRichiestePendenti rimangono uguali, li ometto per brevità ma devono esserci) ...
    // Riporto i metodi modificati per il debug:

    @Transactional
    fun creaAgente(request: CreateAgenteRequest): AgenteDTO {
        val agenziaUUID = UUID.fromString(request.agenziaId)
        val agenzia = agenziaRepository.findById(agenziaUUID)
            .orElseThrow { RuntimeException("Agenzia non trovata") }

        if (request.isCapo && agenzia.agenti.any { it.isCapo }) {
            throw RuntimeException("Questa agenzia ha già un capo.")
        }

        val nuovoAgente = AgenteEntity(
            nome = request.nome,
            cognome = request.cognome,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            agenzia = agenzia,
            isCapo = request.isCapo
        )

        val salvato = agenteRepository.save(nuovoAgente)
        return salvato.toDTO()
    }

    @Transactional(readOnly = true)
    fun getAgenteById(uuid: UUID): AgenteEntity {
        return agenteRepository.findById(uuid)
            .orElseThrow { RuntimeException("Agente con ID $uuid non trovato") }
    }

    @Transactional(readOnly = true)
    fun getAllAgenti(): List<AgenteEntity> {
        return agenteRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getRichiestePendenti(agenteId: String): List<ImmobileDTO> {
        println("\n>>> SERVICE ENTRY: getRichiestePendenti chiamato con ID: '$agenteId' <<<")
        val uuid = try { UUID.fromString(agenteId) } catch (e: Exception) { throw EntityNotFoundException("ID Agente non valido") }
        val agente = agenteRepository.findById(uuid).orElseThrow { EntityNotFoundException("Agente non trovato nel DB") }
        val agenziaId = agente.agenzia?.uuid ?: throw EntityNotFoundException("ID Agenzia è NULL")
        println("   > Agenzia Target: ${agente.agenzia?.nome} (ID: $agenziaId)")
        val immobili = immobileRepository.findRichiestePendentiPerAgenzia(agenziaId)
        println("   > Query Result: ${immobili.size} immobili trovati.")
        println(">>> SERVICE EXIT <<<\n")
        return immobili.map { it.toDto() }
    }

    // --- ACCETTA INCARICO (DEBUGGATO) ---
    @Transactional
    fun accettaIncarico(agenteId: String, immobileId: String) {
        println("\n--- DEBUG ACCETTA INCARICO ---")
        println("1. Richiesta da AgenteID: $agenteId per ImmobileID: $immobileId")

        val uuidAgente = try { UUID.fromString(agenteId) } catch (e: Exception) {
            println("!!! ERRORE: ID Agente non è un UUID valido")
            throw e
        }
        val uuidImmobile = try { UUID.fromString(immobileId) } catch (e: Exception) {
            println("!!! ERRORE: ID Immobile non è un UUID valido")
            throw e
        }

        val agente = agenteRepository.findById(uuidAgente)
            .orElseThrow {
                println("!!! ERRORE: Agente non trovato nel DB")
                EntityNotFoundException("Agente non trovato")
            }
        println("2. Agente trovato: ${agente.email}")

        val immobile = immobileRepository.findById(uuidImmobile)
            .orElseThrow {
                println("!!! ERRORE: Immobile non trovato nel DB")
                EntityNotFoundException("Immobile non trovato")
            }
        println("3. Immobile trovato: ${immobile.indirizzo}")

        // Check Agenzia
        if (immobile.agenzia?.uuid != agente.agenzia?.uuid) {
            println("!!! ERRORE: Mismatch Agenzia. Immobile: ${immobile.agenzia?.uuid}, Agente: ${agente.agenzia?.uuid}")
            throw RuntimeException("Questo immobile non appartiene alla tua agenzia")
        }

        immobile.agente = agente
        val saved = immobileRepository.save(immobile)
        println("4. SUCCESSO: Immobile aggiornato. Agente assegnato: ${saved.agente?.email}")
        println("--- DEBUG END ---\n")
    }

    // --- RIFIUTA INCARICO (DEBUGGATO) ---
    @Transactional
    fun rifiutaIncarico(agenteId: String, immobileId: String) {
        println("\n--- DEBUG RIFIUTA INCARICO ---")
        val uuidAgente = UUID.fromString(agenteId)
        val agente = agenteRepository.findById(uuidAgente).orElseThrow { EntityNotFoundException("Agente non trovato") }
        val immobile = immobileRepository.findById(UUID.fromString(immobileId)).orElseThrow { EntityNotFoundException("Immobile non trovato") }

        if (immobile.agenzia?.uuid != agente.agenzia?.uuid) {
            throw RuntimeException("Questo immobile non appartiene alla tua agenzia")
        }

        println("1. Invia notifica rifiuto a ${immobile.proprietario.email}")
        notificaService.inviaNotifica(
            destinatario = immobile.proprietario,
            titolo = "Richiesta Inserimento Rifiutata",
            corpo = "Ci dispiace, ma la tua richiesta per l'immobile in ${immobile.indirizzo} non può essere gestita dalla nostra agenzia."
        )

        println("2. Eliminazione immobile...")
        immobileRepository.delete(immobile)
        println("3. SUCCESSO: Immobile eliminato")
        println("--- DEBUG END ---\n")
    }
}
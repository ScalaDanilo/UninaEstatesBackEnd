package com.dieti.backend.service

import com.dieti.backend.dto.LoginRequest
import com.dieti.backend.dto.UtenteRegistrazioneRequest
import com.dieti.backend.dto.UtenteResponseDTO
import com.dieti.backend.dto.toDto
import com.dieti.backend.dto.toEntity
import com.dieti.backend.repository.AgenteRepository
import com.dieti.backend.repository.UtenteRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val utenteRepository: UtenteRepository,
    private val agenteRepository: AgenteRepository, // Iniettiamo il repo agenti
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun registraUtente(request: UtenteRegistrazioneRequest): UtenteResponseDTO {
        // La registrazione crea SOLO Utenti normali
        if (utenteRepository.existsByEmail(request.email) || agenteRepository.findByEmail(request.email) != null) {
            throw RuntimeException("Email già registrata!")
        }

        val nuovaEntity = request.toEntity(passwordEncoder)
        val entitySalvata = utenteRepository.save(nuovaEntity)
        return entitySalvata.toDto()
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): UtenteResponseDTO {
        println("DEBUG LOGIN: Tentativo per ${request.email}")

        // 1. CONTROLLO TABELLA AGENTI (Priorità Manager)
        val agente = agenteRepository.findByEmail(request.email)
        if (agente != null) {
            if (passwordEncoder.matches(request.password, agente.password)) {
                println("DEBUG LOGIN: Accesso effettuato come MANAGER")
                return agente.toDto() // Restituisce ruolo "MANAGER"
            } else {
                throw RuntimeException("Password errata")
            }
        }

        // 2. CONTROLLO TABELLA UTENTI (Se non è agente)
        val utente = utenteRepository.findByEmail(request.email)
        if (utente != null) {
            if (passwordEncoder.matches(request.password, utente.password)) {
                println("DEBUG LOGIN: Accesso effettuato come UTENTE")
                return utente.toDto() // Restituisce ruolo "UTENTE"
            } else {
                throw RuntimeException("Password errata")
            }
        }

        // 3. NESSUNO DEI DUE
        throw RuntimeException("Email non trovata")
    }
}
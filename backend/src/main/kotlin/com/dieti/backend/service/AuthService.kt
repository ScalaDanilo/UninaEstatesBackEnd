package com.dieti.backend.service

import com.dieti.backend.dto.LoginRequest
import com.dieti.backend.dto.UtenteRegistrazioneRequest
import com.dieti.backend.dto.UtenteResponseDTO
import com.dieti.backend.dto.toDto
import com.dieti.backend.dto.toEntity
import com.dieti.backend.repository.AgenteRepository
import com.dieti.backend.repository.AmministratoreRepository
import com.dieti.backend.repository.UtenteRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val utenteRepository: UtenteRepository,
    private val agenteRepository: AgenteRepository,
    private val amministratoreRepository: AmministratoreRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun registraUtente(request: UtenteRegistrazioneRequest): UtenteResponseDTO {
        if (utenteRepository.existsByEmail(request.email) ||
            agenteRepository.findByEmail(request.email) != null ||
            amministratoreRepository.findByEmail(request.email) != null) {
            throw RuntimeException("Email già registrata!")
        }

        val nuovaEntity = request.toEntity(passwordEncoder)
        val entitySalvata = utenteRepository.save(nuovaEntity)
        return entitySalvata.toDto()
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): UtenteResponseDTO {
        println("DEBUG LOGIN: Cerco utente ${request.email} in tutte le tabelle...")

        // 1. CONTROLLO ADMIN
        val admin = amministratoreRepository.findByEmail(request.email)
        if (admin != null) {
            if (passwordEncoder.matches(request.password, admin.password)) {
                return UtenteResponseDTO(
                    id = admin.uuid.toString(), // Questo ID sarà il token
                    nome = "Amministratore",
                    cognome = "Sistema",
                    email = admin.email,
                    telefono = null,
                    ruolo = "ADMIN",
                    preferiti = emptyList(),
                    agenziaNome = null
                )
            } else throw RuntimeException("Password errata (Admin)")
        }

        // 2. CONTROLLO AGENTE (MANAGER)
        val agente = agenteRepository.findByEmail(request.email)
        if (agente != null) {
            if (passwordEncoder.matches(request.password, agente.password)) {
                return agente.toDto()
            } else throw RuntimeException("Password errata (Agente)")
        }

        // 3. CONTROLLO UTENTE
        val utente = utenteRepository.findByEmail(request.email)
        if (utente != null) {
            if (passwordEncoder.matches(request.password, utente.password)) {
                return utente.toDto()
            } else throw RuntimeException("Password errata (Utente)")
        }

        throw RuntimeException("Email non trovata in nessun database")
    }
}
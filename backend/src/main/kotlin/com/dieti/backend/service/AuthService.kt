package com.dieti.backend.service

import com.dieti.backend.LoginRequest
import com.dieti.backend.UtenteRegistrazioneRequest
import com.dieti.backend.UtenteResponseDTO
import com.dieti.backend.repository.UtenteRepository
import com.dieti.backend.toDto
import com.dieti.backend.toEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val utenteRepository: UtenteRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun registraUtente(request: UtenteRegistrazioneRequest): UtenteResponseDTO {
        // 1. Controllo validità
        if (utenteRepository.existsByEmail(request.email)) {
            throw RuntimeException("Email già registrata!")
            // In un'app reale useresti un'eccezione personalizzata (es. 409 Conflict)
        }

        // 2. Conversione DTO -> Entity (con password criptata)
        val nuovaEntity = request.toEntity(passwordEncoder)

        // 3. Salvataggio nel DB
        val entitySalvata = utenteRepository.save(nuovaEntity)

        // 4. Conversione Entity -> DTO (senza password) per la risposta
        return entitySalvata.toDto()
    }
    @Transactional(readOnly = true)
    fun login(request: LoginRequest): UtenteResponseDTO {
        val utente = utenteRepository.findByEmail(request.email)
            ?: throw RuntimeException("Email non trovata") // Questo genererebbe errore 500

        // DEBUG: Stampa per vedere cosa succede
        println("DEBUG PASSWORD: DB=${utente.password} vs INPUT=${request.password}")

        if (!passwordEncoder.matches(request.password, utente.password)) {
            println("DEBUG: Password non corrispondono!")
            throw RuntimeException("Password errata") // Genera errore 500
        }

        return utente.toDto()
    }
}
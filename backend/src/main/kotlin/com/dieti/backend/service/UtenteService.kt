package com.dieti.backend.service

import com.dieti.backend.entity.UtenteRegistratoEntity
import com.dieti.backend.repository.UtenteRepository
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class UtenteService(private val utenteRepository: UtenteRepository) {

    // Recupera un utente per ID
    fun getUtenteById(uuid: UUID): UtenteRegistratoEntity {
        return utenteRepository.findById(uuid).orElseThrow {
            RuntimeException("Utente non trovato con ID: $uuid")
        }
    }

    // Recupera un utente per Email (utile per il login o controlli)
    fun getUtenteByEmail(email: String): UtenteRegistratoEntity? {
        return utenteRepository.findByEmail(email)
    }

    // Altri metodi esistenti (es. registrazione)...
}
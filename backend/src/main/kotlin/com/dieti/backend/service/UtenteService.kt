package com.dieti.backend.service

import com.dieti.backend.entity.UtenteRegistratoEntity
import com.dieti.backend.repository.ImmobileRepository // Assumendo esista
import com.dieti.backend.repository.UtenteRepository
import org.hibernate.Hibernate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UtenteService(
    private val utenteRepository: UtenteRepository,
    private val immobileRepository: ImmobileRepository
) {

    // --- LETTURA PROFILO ---
    @Transactional(readOnly = true)
    fun getUtenteById(uuid: UUID): UtenteRegistratoEntity {
        val utente = utenteRepository.findById(uuid).orElseThrow {
            RuntimeException("Utente non trovato con ID: $uuid")
        }

        Hibernate.initialize(utente.preferiti)

        return utente
    }

    // --- METODO ELIMINAZIONE AGGIORNATO ---
    @Transactional
    fun eliminaUtente(uuid: UUID) {
        val utente = utenteRepository.findById(uuid).orElseThrow {
            RuntimeException("Impossibile eliminare: Utente non trovato")
        }

        immobileRepository.deleteByProprietario(utente)

        utenteRepository.delete(utente)
    }

    // --- AGGIUNGI PREFERITO ---
    @Transactional
    fun aggiungiPreferito(userId: UUID, immobileId: UUID) {
        val utente = utenteRepository.findById(userId).orElseThrow {
            RuntimeException("Utente non trovato")
        }
        val immobile = immobileRepository.findById(immobileId).orElseThrow {
            RuntimeException("Immobile non trovato")
        }

        // Aggiungiamo alla lista se non c'è già
        if (!utente.preferiti.contains(immobile)) {
            utente.preferiti.add(immobile)
            utenteRepository.save(utente)
        }
    }

    // --- RIMUOVI PREFERITO ---
    @Transactional
    fun rimuoviPreferito(userId: UUID, immobileId: UUID) {
        val utente = utenteRepository.findById(userId).orElseThrow {
            RuntimeException("Utente non trovato")
        }

        utente.preferiti.removeIf { it.uuid == immobileId }
        utenteRepository.save(utente)
    }

    fun getUtenteByEmail(email: String): UtenteRegistratoEntity? {
        return utenteRepository.findByEmail(email)
    }
}
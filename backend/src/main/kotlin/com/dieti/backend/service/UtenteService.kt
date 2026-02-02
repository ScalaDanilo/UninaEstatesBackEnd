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

    // --- LETTURA PROFILO (CON FIX LAZY LOADING) ---
    @Transactional(readOnly = true)
    fun getUtenteById(uuid: UUID): UtenteRegistratoEntity {
        val utente = utenteRepository.findById(uuid).orElseThrow {
            RuntimeException("Utente non trovato con ID: $uuid")
        }

        // FIX CRITICO: Inizializziamo i preferiti mentre la sessione è aperta.
        // Hibernate.initialize forza il caricamento dei dati dalla tabella di join.
        Hibernate.initialize(utente.preferiti)

        return utente
    }

    // --- METODO ELIMINAZIONE AGGIORNATO ---
    @Transactional
    fun eliminaUtente(uuid: UUID) {
        // 1. Recuperiamo l'utente (ci serve l'entità completa per il deleteByProprietario)
        val utente = utenteRepository.findById(uuid).orElseThrow {
            RuntimeException("Impossibile eliminare: Utente non trovato")
        }

        // 2. PULIZIA: Eliminiamo prima tutti gli immobili posseduti da questo utente
        // Se non lo facciamo, il DB bloccherà la cancellazione dell'utente (Foreign Key Error)
        immobileRepository.deleteByProprietario(utente)

        // (Opzionale: Se avessi appuntamenti/offerte, dovresti cancellare anche quelli qui)

        // 3. Ora possiamo eliminare l'utente senza errori
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
            utenteRepository.save(utente) // Spring aggiornerà automaticamente la tabella "preferiti"
        }
    }

    // --- RIMUOVI PREFERITO ---
    @Transactional
    fun rimuoviPreferito(userId: UUID, immobileId: UUID) {
        val utente = utenteRepository.findById(userId).orElseThrow {
            RuntimeException("Utente non trovato")
        }

        // Rimuoviamo l'immobile dalla lista (basta l'ID per trovarlo o l'oggetto)
        utente.preferiti.removeIf { it.uuid == immobileId }
        utenteRepository.save(utente)
    }

    // Recupera per Login
    fun getUtenteByEmail(email: String): UtenteRegistratoEntity? {
        return utenteRepository.findByEmail(email)
    }
}
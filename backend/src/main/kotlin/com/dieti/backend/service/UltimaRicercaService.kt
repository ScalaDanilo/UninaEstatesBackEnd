package com.dieti.backend.service

import com.dieti.backend.entity.UltimaRicercaEntity
import com.dieti.backend.repository.UltimaRicercaRepository
import com.dieti.backend.repository.UtenteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class UltimaRicercaService(
    private val ultimaRicercaRepository: UltimaRicercaRepository,
    private val utenteRepository: UtenteRepository
) {

    // --- SALVATAGGIO  ---
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun salvaRicerca(query: String, emailUtente: String) {
        if (query.isBlank()) return

        try {
            val utente = utenteRepository.findByEmail(emailUtente) ?: return
            val ricercheEsistenti = ultimaRicercaRepository.findAllByUtenteRegistratoEmailOrderByDataDesc(emailUtente)

            val ricercaEsistente = ricercheEsistenti.find { it.corpo.equals(query, ignoreCase = true) }
            if (ricercaEsistente != null) {
                ricercaEsistente.data = LocalDate.now()
                ultimaRicercaRepository.save(ricercaEsistente)
                return
            }

            if (ricercheEsistenti.size >= 10) {
                val daEliminare = ricercheEsistenti.last()
                ultimaRicercaRepository.delete(daEliminare)
            }

            val nuovaRicerca = UltimaRicercaEntity(
                corpo = query,
                data = LocalDate.now(),
                utenteRegistrato = utente
            )
            ultimaRicercaRepository.save(nuovaRicerca)
        } catch (e: Exception) {
            println("WARNING: Errore salvataggio cronologia: ${e.message}")
        }
    }

    // --- RECUPERO ---
    @Transactional(readOnly = true)
    fun getRicercheRecenti(emailUtente: String): List<String> {
        return ultimaRicercaRepository.findAllByUtenteRegistratoEmailOrderByDataDesc(emailUtente)
            .mapNotNull { it.corpo }
    }

    // --- CANCELLAZIONE SINGOLA ---
    @Transactional
    fun cancellaRicerca(query: String, emailUtente: String) {
        val ricerca = ultimaRicercaRepository.findByUtenteRegistratoEmailAndCorpoIgnoreCase(emailUtente, query)
        if (ricerca != null) {
            ultimaRicercaRepository.delete(ricerca)
        }
    }
}
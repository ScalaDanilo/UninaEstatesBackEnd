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

    /**
     * FIX CRITICO: `propagation = Propagation.REQUIRES_NEW`
     * Questo crea una nuova transazione separata per il salvataggio della cronologia.
     * Se questo metodo fallisce (es. errore colonne DB), la transazione principale
     * (quella che recupera gli immobili) RIMANE VALIDA e l'utente vede i risultati.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun salvaRicerca(query: String, emailUtente: String) {
        try {
            if (query.isBlank()) return

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
            // Logghiamo l'errore ma non facciamo crashare nulla.
            // Grazie a REQUIRES_NEW, questo catch protegge la ricerca degli immobili.
            println("WARNING: Impossibile salvare la ricerca: ${e.message}")
        }
    }
}
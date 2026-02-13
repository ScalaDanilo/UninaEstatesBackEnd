package com.dieti.backend.service

import com.dieti.backend.entity.UtenteRegistratoEntity
import com.dieti.backend.repository.UltimaRicercaRepository
import com.dieti.backend.repository.UtenteRepository
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FirebaseNotificationService(
    private val utenteRepository: UtenteRepository,
    private val ultimaRicercaRepository: UltimaRicercaRepository
) {

    // Aggiorna il token quando l'utente fa login da app
    @Transactional
    fun updateFcmToken(userId: String, token: String) {
        val utente = utenteRepository.findById(UUID.fromString(userId)).orElse(null)
        if (utente != null) {
            utente.fcmToken = token
            utenteRepository.save(utente)
        }
    }

    // Aggiorna le preferenze dal profilo
    @Transactional
    fun updatePreferences(userId: String, trattative: Boolean, pubblicazione: Boolean, nuoviImmobili: Boolean) {
        val utente = utenteRepository.findById(UUID.fromString(userId)).orElse(null)
        if (utente != null) {
            utente.notifTrattative = trattative
            utente.notifPubblicazione = pubblicazione
            utente.notifNuoviImmobili = nuoviImmobili
            utenteRepository.save(utente)
        }
    }

    // 1. Notifica Singola (es. Trattativa o Esito Pubblicazione)
    fun sendNotificationToUser(utente: UtenteRegistratoEntity, title: String, body: String, checkPreference: (UtenteRegistratoEntity) -> Boolean) {
        // Controllo se l'utente ha il token E se ha attivato quella specifica notifica
        if (utente.fcmToken.isNullOrBlank() || !checkPreference(utente)) return

        try {
            val message = Message.builder()
                .setToken(utente.fcmToken)
                .setNotification(
                    Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build()
                )
                .putData("click_action", "FLUTTER_NOTIFICATION_CLICK") // Utile per gestire il click lato client
                .build()

            FirebaseMessaging.getInstance().send(message)
            println("Notifica inviata a ${utente.email}: $title")
        } catch (e: Exception) {
            println("Errore invio FCM a ${utente.email}: ${e.message}")
        }
    }

    // 2. Notifica Broadcast Smart (Nuovo Immobile in zona)
    @Transactional(readOnly = true)
    fun notifyUsersForNewProperty(localita: String, indirizzo: String) {
        if (localita.isBlank()) return

        // TROVA GLI UTENTI INTERESSATI
        // Logica: Prendi tutte le ricerche recenti che contengono la località
        // E che appartengono a utenti che hanno la notifica attiva
        val ricerche = ultimaRicercaRepository.findAll() // Nota: ottimizzabile con query custom SQL per performance
        
        val utentiTarget = ricerche
            .filter { it.corpo?.contains(localita, ignoreCase = true) == true }
            .mapNotNull { it.utenteRegistrato }
            .distinctBy { it.uuid } // Evita doppi invii allo stesso utente
            .filter { it.notifNuoviImmobili && !it.fcmToken.isNullOrBlank() }

        if (utentiTarget.isEmpty()) return

        val tokens = utentiTarget.map { it.fcmToken!! }

        // Firebase permette max 500 token per volta nel multicast, gestiamo i batch se necessario
        tokens.chunked(500).forEach { batchTokens ->
            try {
                val message = MulticastMessage.builder()
                    .addAllTokens(batchTokens)
                    .setNotification(
                        Notification.builder()
                            .setTitle("Nuovo Immobile a $localita! 🏠")
                            .setBody("È appena stato pubblicato un immobile in $indirizzo. Scoprilo subito!")
                            .build()
                    )
                    .build()

                val response = FirebaseMessaging.getInstance().sendEachForMulticast(message)
                println("Batch notifica nuovi immobili: ${response.successCount} successi su ${batchTokens.size}")
            } catch (e: Exception) {
                println("Errore multicast FCM: ${e.message}")
            }
        }
    }
}
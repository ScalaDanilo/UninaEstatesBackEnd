package com.dieti.backend.service

import com.dieti.backend.entity.UtenteRegistratoEntity
import com.dieti.backend.repository.UltimaRicercaRepository
import com.dieti.backend.repository.UtenteRepository
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FirebaseNotificationService(
    private val utenteRepository: UtenteRepository,
    private val ultimaRicercaRepository: UltimaRicercaRepository
) {

    private val logger = LoggerFactory.getLogger(FirebaseNotificationService::class.java)

    // Aggiorna il token quando l'utente fa login da app
    @Transactional
    fun updateFcmToken(userId: String, token: String) {
        val utente = utenteRepository.findById(UUID.fromString(userId)).orElse(null)
        if (utente != null) {
            // Log per verificare se il token sta cambiando
            logger.info(">>> AGGIORNAMENTO TOKEN per ${utente.email}: ${token.take(10)}...")
            utente.fcmToken = token
            utenteRepository.save(utente)
        }
    }

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

    private fun ensureFirebaseInitialized() {
        if (FirebaseApp.getApps().isEmpty()) {
            logger.warn(">>> FirebaseApp non trovata. Tentativo di inizializzazione LAZY...")
            try {
                val inputStream = this::class.java.classLoader.getResourceAsStream("firebase-service-account.json")
                    ?: throw RuntimeException("File firebase-service-account.json non trovato nel classpath")

                // Usiamo ServiceAccountCredentials per estrarre l'ID del progetto
                val credentials = ServiceAccountCredentials.fromStream(inputStream)

                val options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build()

                FirebaseApp.initializeApp(options)
                logger.info(">>> Firebase inizializzato con successo.")
                logger.info(">>> USANDO CREDENZIALI FIREBASE PER IL PROGETTO: ${credentials.projectId}")
                logger.warn(">>> VERIFICA: Assicurati che il 'google-services.json' nell'app Android abbia lo stesso 'project_id'!")

            } catch (e: Exception) {
                logger.error(">>> ERRORE Inizializzazione Firebase Lazy: ${e.message}", e)
            }
        }
    }

    fun sendNotificationToUser(utente: UtenteRegistratoEntity, title: String, body: String, checkPreference: (UtenteRegistratoEntity) -> Boolean) {
        // --- LOGICA DI DEBUG AVANZATA ---
        if (utente.fcmToken.isNullOrBlank()) {
            logger.warn(">>> ABORT PUSH: L'utente ${utente.email} NON ha un token FCM salvato nel database.")
            return
        }

        if (!checkPreference(utente)) {
            logger.warn(">>> ABORT PUSH: L'utente ${utente.email} ha le notifiche DISABILITATE per questa categoria.")
            return
        }
        // -------------------------------

        ensureFirebaseInitialized()

        if (FirebaseApp.getApps().isEmpty()) {
            logger.error(">>> ERRORE CRITICO: FirebaseApp NON inizializzata.")
            return
        }

        logger.info(">>> TENTATIVO INVIO PUSH a: ${utente.email}")
        // Stampiamo il token completo per debug se necessario, o parziale per sicurezza
        logger.debug(">>> TOKEN TARGET: ${utente.fcmToken}")

        try {
            val message = Message.builder()
                .setToken(utente.fcmToken)
                .setNotification(
                    Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build()
                )
                .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                .build()

            FirebaseMessaging.getInstance().send(message)
            logger.info(">>> PUSH INVIATA con successo a: ${utente.email}")

        } catch (e: FirebaseMessagingException) {
            logger.error(">>> ERRORE FIREBASE CODE: ${e.messagingErrorCode}")
            logger.error(">>> ERRORE FIREBASE MSG: ${e.message}")

            if (e.message?.contains("Requested entity was not found") == true ||
                e.messagingErrorCode == MessagingErrorCode.UNREGISTERED ||
                e.messagingErrorCode == MessagingErrorCode.INVALID_ARGUMENT) {

                logger.warn(">>> DIAGNOSI CRITICA: Il token non è valido per questo progetto Firebase.")
                logger.warn(">>> CAUSA 1: Disallineamento Progetti (Backend usa credenziali diverse dall'App).")
                logger.warn(">>> CAUSA 2: Token scaduto o appartenente a una vecchia installazione.")
                logger.warn(">>> SOLUZIONE: Fai logout/login nell'app per aggiornare il token nel DB.")
            }
        } catch (e: Exception) {
            logger.error(">>> ERRORE GENERICO INVIO PUSH: ${e.message}")
        }
    }

    @Transactional(readOnly = true)
    fun notifyUsersForNewProperty(localita: String, indirizzo: String) {
        if (localita.isBlank()) return

        ensureFirebaseInitialized()

        if (FirebaseApp.getApps().isEmpty()) return

        val ricerche = ultimaRicercaRepository.findAll()

        val utentiTarget = ricerche
            .filter { it.corpo?.contains(localita, ignoreCase = true) == true }
            .mapNotNull { it.utenteRegistrato }
            .distinctBy { it.uuid }
            .filter { it.notifNuoviImmobili && !it.fcmToken.isNullOrBlank() }

        if (utentiTarget.isEmpty()) return

        val tokens = utentiTarget.map { it.fcmToken!! }

        tokens.chunked(500).forEach { batchTokens ->
            try {
                val message = MulticastMessage.builder()
                    .addAllTokens(batchTokens)
                    .setNotification(
                        Notification.builder()
                            .setTitle("Nuovo Immobile a $localita! 🏠")
                            .setBody("È appena stato pubblicato un immobile in $indirizzo.")
                            .build()
                    )
                    .build()

                val response = FirebaseMessaging.getInstance().sendEachForMulticast(message)
                logger.info(">>> Batch notifica nuovi immobili: ${response.successCount} successi su ${batchTokens.size}")
            } catch (e: Exception) {
                logger.error(">>> Errore multicast FCM: ${e.message}")
            }
        }
    }
}
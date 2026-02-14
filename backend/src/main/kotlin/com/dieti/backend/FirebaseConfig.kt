package com.dieti.backend.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.IOException

@Configuration
class FirebaseConfig {

    @PostConstruct
    fun initialize() {
        try {
            // Controlla se esiste già un'app inizializzata per evitare doppi avvii
            if (FirebaseApp.getApps().isEmpty()) {
                val serviceAccount = ClassPathResource("firebase-service-account.json").inputStream

                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()

                FirebaseApp.initializeApp(options)
                println(">>> FIREBASE INIZIALIZZATO CON SUCCESSO! <<<")
            }
        } catch (e: IOException) {
            System.err.println(">>> ERRORE FATALE: Impossibile leggere firebase-service-account.json")
            e.printStackTrace()
        } catch (e: Exception) {
            System.err.println(">>> ERRORE INIZIALIZZAZIONE FIREBASE: ${e.message}")
        }
    }
}
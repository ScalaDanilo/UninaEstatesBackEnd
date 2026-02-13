package com.dieti.backend

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.IOException

@Configuration
class FirebaseConfig {

    @Bean
    fun firebaseApp(): FirebaseApp {
        return try {
            val serviceAccount = ClassPathResource("firebase-service-account.json").inputStream

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            } else {
                FirebaseApp.getInstance()
            }
        } catch (e: IOException) {
            throw RuntimeException("Errore nel caricamento del file firebase-service-account.json. Assicurati che sia in src/main/resources", e)
        }
    }
}
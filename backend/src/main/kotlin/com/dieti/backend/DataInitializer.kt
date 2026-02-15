package com.dieti.backend

import com.dieti.backend.entity.AmministratoreEntity
import com.dieti.backend.repository.AmministratoreRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val amministratoreRepository: AmministratoreRepository,
    private val passwordEncoder: PasswordEncoder,
    // Iniettiamo i valori dal file application.properties
    @Value("\${app.admin.default-email}") private val adminEmail: String,
    @Value("\${app.admin.default-password}") private val adminPassword: String
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        // Usiamo la variabile iniettata invece della stringa hardcoded
        if (amministratoreRepository.findByEmail(adminEmail) == null) {
            println(">>> INIZIALIZZAZIONE: Creazione Amministratore Default...")

            val adminDefault = AmministratoreEntity(
                email = adminEmail,
                password = passwordEncoder.encode(adminPassword) // Password presa dalle properties
            )

            amministratoreRepository.save(adminDefault)
            println(">>> ADMIN CREATO: Email='$adminEmail'")
            // Non stampiamo la password nei log per sicurezza!
        }
    }
}
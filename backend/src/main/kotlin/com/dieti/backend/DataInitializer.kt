package com.dieti.backend

import com.dieti.backend.entity.AmministratoreEntity
import com.dieti.backend.repository.AmministratoreRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val amministratoreRepository: AmministratoreRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        // Controlla se esiste già un admin (per evitare duplicati al riavvio)
        if (amministratoreRepository.findByEmail("admin") == null) {
            println(">>> INIZIALIZZAZIONE: Creazione Amministratore Default...")
            
            val adminDefault = AmministratoreEntity(
                email = "admin",
                password = passwordEncoder.encode("admin123") // Criptiamo la password
            )
            
            amministratoreRepository.save(adminDefault)
            println(">>> ADMIN CREATO: Email='admin', Password='admin123'")
        }
    }
}
package com.dieti.backend.dto

// Per la registrazione classica (Email/Password)
data class RegistrazioneRequest(
    val nome: String,
    val cognome: String,
    val email: String,
    val password: String,
    val telefono: String?
)

// Per il login classico
data class LoginRequest(
    val email: String,
    val password: String
)

// Cosa restituisce il server dopo il login/registrazione
data class UtenteResponse(
    val uuid: String,
    val nome: String,
    val email: String,
    val token: String? = null // Opzionale per il futuro
)
package com.dieti.backend.dto

data class AppuntamentoRequest(
    val userId: String,      // Chi prenota
    val immobileId: String,  // Quale casa
    val data: String,        // Formato "2024-12-31"
    val ora: String,         // Formato "15:30"
    val corpo: String?       // Messaggio opzionale
)

data class OffertaRequest(
    val offerenteId: String, // Chi compra
    val immobileId: String,  // Quale casa
    val prezzo: Int,
    val corpo: String?       // Messaggio opzionale
)

data class RispostaOffertaRequest(
    val offertaId: Int,      // ID dell'offerta
    val accettata: Boolean,  // true = accetta, false = rifiuta/controfferta
    val messaggio: String?
)

data class NotificaDTO(
    val titolo: String,
    val corpo: String?,
    val data: String
)
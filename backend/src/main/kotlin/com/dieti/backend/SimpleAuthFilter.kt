package com.dieti.backend.config

import com.dieti.backend.repository.UtenteRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Collections

@Component
class SimpleAuthFilter(
    private val utenteRepository: UtenteRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 1. Cerchiamo l'header "X-Auth-Email" nella richiesta
        val userEmail = request.getHeader("X-Auth-Email")

        // --- LOG DI DEBUG ---
        if (userEmail != null) {
            println("DEBUG FILTER: Ricevuta email header: $userEmail")
            val utente = utenteRepository.findByEmail(userEmail)
            if (utente == null) {
                println("DEBUG FILTER: ATTENZIONE! Utente non trovato nel DB per email: $userEmail -> Auth fallita")
            } else {
                println("DEBUG FILTER: Utente trovato! ID: ${utente.uuid}")
                // ... logica di autenticazione esistente ...
                val auth = UsernamePasswordAuthenticationToken(
                    utente.email,
                    null,
                    Collections.emptyList()
                )
                SecurityContextHolder.getContext().authentication = auth
            }
        } else {
            println("DEBUG FILTER: Nessun header X-Auth-Email trovato")
        }

        // Continua con la catena di filtri
        filterChain.doFilter(request, response)
    }
}
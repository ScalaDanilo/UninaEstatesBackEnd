package com.dieti.backend.config

import com.dieti.backend.repository.AgenteRepository
import com.dieti.backend.repository.AmministratoreRepository
import com.dieti.backend.repository.UtenteRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class SimpleAuthFilter(
    private val utenteRepository: UtenteRepository,
    private val agenteRepository: AgenteRepository,
    private val amministratoreRepository: AmministratoreRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7) // Rimuove "Bearer "

            try {
                // Il token è l'UUID dell'utente
                val userId = UUID.fromString(token)
                println("DEBUG FILTER: Token ricevuto (UUID): $userId")

                // A. AMMINISTRATORI
                val admin = amministratoreRepository.findById(userId).orElse(null)
                if (admin != null) {
                    setAuthentication(admin.uuid.toString(), "ROLE_ADMIN")
                    filterChain.doFilter(request, response)
                    return
                }

                // B. AGENTI
                val agente = agenteRepository.findById(userId).orElse(null)
                if (agente != null) {
                    setAuthentication(agente.uuid.toString(), "ROLE_MANAGER")
                    filterChain.doFilter(request, response)
                    return
                }

                // C. UTENTI
                val utente = utenteRepository.findById(userId).orElse(null)
                if (utente != null) {
                    setAuthentication(utente.uuid.toString(), "ROLE_USER")
                    filterChain.doFilter(request, response)
                    return
                }

                println("DEBUG FILTER: Nessun utente trovato per l'ID: $userId")

            } catch (e: IllegalArgumentException) {
                println("DEBUG FILTER: Token non valido (non è un UUID): $token")
            }
        } else {
            // println("DEBUG FILTER: Header Authorization mancante o formato errato")
        }

        filterChain.doFilter(request, response)
    }

    private fun setAuthentication(principalId: String, role: String) {
        println("DEBUG FILTER: Autenticazione OK. ID: $principalId, Ruolo: $role")
        val auth = UsernamePasswordAuthenticationToken(
            principalId, // IMPORTANTISSIMO: Il principal ora è l'ID
            null,
            listOf(SimpleGrantedAuthority(role))
        )
        SecurityContextHolder.getContext().authentication = auth
    }
}
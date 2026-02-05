package com.dieti.backend.config

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

@Component
class SimpleAuthFilter(
    private val utenteRepository: UtenteRepository,
    private val amministratoreRepository: AmministratoreRepository // Aggiunto repository admin
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 1. Cerchiamo l'header
        val emailHeader = request.getHeader("X-Auth-Email")

        if (emailHeader != null) {
            println("DEBUG FILTER: Ricevuta email header: $emailHeader")

            // 2a. Cerca tra gli UTENTI REGISTRATI
            val utente = utenteRepository.findByEmail(emailHeader)

            if (utente != null) {
                println("DEBUG FILTER: Utente trovato! ID: ${utente.uuid}")
                val auth = UsernamePasswordAuthenticationToken(
                    utente.email,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_USER"))
                )
                SecurityContextHolder.getContext().authentication = auth
            }
            // 2b. Se non è utente, cerca tra gli AMMINISTRATORI
            else {
                val admin = amministratoreRepository.findByEmail(emailHeader)
                if (admin != null) {
                    println("DEBUG FILTER: Amministratore trovato! ID: ${admin.uuid}")
                    val auth = UsernamePasswordAuthenticationToken(
                        admin.email,
                        null,
                        listOf(SimpleGrantedAuthority("ROLE_ADMIN"))
                    )
                    SecurityContextHolder.getContext().authentication = auth
                } else {
                    println("DEBUG FILTER: ATTENZIONE! Nessun account trovato per email: $emailHeader")
                }
            }
        } else {
            // println("DEBUG FILTER: Nessun header X-Auth-Email trovato")
        }

        filterChain.doFilter(request, response)
    }
}
package com.dieti.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // 1. Disabilita la protezione CSRF (Fondamentale per le API REST)
            .csrf { it.disable() }

            // 2. Disabilita il login standard di Spring (pagina HTML di login)
            .formLogin { it.disable() }
            .httpBasic { it.disable() }

            // 3. Gestione Autorizzazioni (L'ordine conta!)
            .authorizeHttpRequests { auth ->
                auth
                    // Lascia passare TUTTO quello che arriva su /auth/...
                    .requestMatchers("/auth/**").permitAll()

                    // Lascia passare le richieste di errore (altrimenti vedi 403 sugli errori)
                    .requestMatchers("/error").permitAll()

                    // Tutto il resto richiede autenticazione (lo gestiremo dopo con i Token)
                    // Per ora, per testare se è un problema di sicurezza, puoi mettere .permitAll() anche qui se vuoi
                    .anyRequest().authenticated()
            }

        return http.build()
    }
}
package com.dieti.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val simpleAuthFilter: SimpleAuthFilter // Iniettiamo il filtro creato sopra
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeHttpRequests { auth ->
                // Endpoint pubblici
                auth.requestMatchers("/auth/**", "/error").permitAll()
                auth.requestMatchers("/api/immagini/**").permitAll() // Importante per Glide/Coil

                // Tutti gli altri endpoint (/api/immobili) richiedono che SimpleAuthFilter abbia fatto il suo lavoro
                auth.anyRequest().authenticated()
            }
            // Inseriamo il nostro filtro PRIMA di quello standard, così intercettiamo l'header
            .addFilterBefore(simpleAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
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
    private val simpleAuthFilter: SimpleAuthFilter
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
                auth.requestMatchers("/api/immagini/**").permitAll()

                // *** FIX: Rendiamo pubblico il login dell'admin ***
                auth.requestMatchers("/api/admin/login").permitAll()

                // Tutti gli altri endpoint richiedono autenticazione
                auth.anyRequest().authenticated()
            }
            .addFilterBefore(simpleAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
package com.dieti.backend.service

import com.dieti.backend.dto.AdminOptionDTO
import com.dieti.backend.dto.ChangeMyPasswordRequest
import com.dieti.backend.dto.CreateAdminRequest
import com.dieti.backend.dto.LoginRequest
import com.dieti.backend.entity.AmministratoreEntity
import com.dieti.backend.repository.AmministratoreRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AmministratoreService(
    private val amministratoreRepository: AmministratoreRepository,
    private val passwordEncoder: PasswordEncoder
) {
    // ... (login, creaAmministratore esistenti) ...
    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AmministratoreEntity {
        val admin = amministratoreRepository.findByEmail(request.email)
            ?: throw RuntimeException("Account amministratore non trovato")
        if (!passwordEncoder.matches(request.password, admin.password)) throw RuntimeException("Password errata")
        return admin
    }

    @Transactional
    fun creaAmministratore(request: CreateAdminRequest): AmministratoreEntity {
        if (amministratoreRepository.findByEmail(request.email) != null) throw IllegalArgumentException("Email già in uso")
        val admin = AmministratoreEntity(email = request.email, password = passwordEncoder.encode(request.password))
        return amministratoreRepository.save(admin)
    }

    // --- NUOVI METODI ---

    @Transactional(readOnly = true)
    fun getAdministratorsOptions(): List<AdminOptionDTO> {
        return amministratoreRepository.findAll().map {
            AdminOptionDTO(it.uuid.toString(), it.email)
        }
    }

    @Transactional
    fun cambiaPasswordPersonale(email: String, request: ChangeMyPasswordRequest) {
        val admin = amministratoreRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Amministratore non trovato")

        if (!passwordEncoder.matches(request.oldPassword, admin.password)) {
            throw IllegalArgumentException("Password attuale non corretta")
        }

        admin.password = passwordEncoder.encode(request.newPassword)
        amministratoreRepository.save(admin)
    }
}
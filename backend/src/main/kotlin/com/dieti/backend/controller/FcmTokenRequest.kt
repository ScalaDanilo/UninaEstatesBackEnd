package com.dieti.backend.controller

import com.dieti.backend.service.FirebaseNotificationService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

data class FcmTokenRequest(val token: String)

data class NotificationPreferencesRequest(
    val notifTrattative: Boolean,
    val notifPubblicazione: Boolean,
    val notifNuoviImmobili: Boolean
)

@RestController
@RequestMapping("/api/notifications")
class NotificationSettingsController(
    private val firebaseService: FirebaseNotificationService
) {

    @PostMapping("/token")
    fun updateToken(@RequestBody request: FcmTokenRequest, authentication: Authentication): ResponseEntity<Void> {
        val userId = authentication.name // UUID
        firebaseService.updateFcmToken(userId, request.token)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/preferences")
    fun updatePreferences(@RequestBody request: NotificationPreferencesRequest, authentication: Authentication): ResponseEntity<Void> {
        val userId = authentication.name // UUID
        firebaseService.updatePreferences(
            userId,
            request.notifTrattative,
            request.notifPubblicazione,
            request.notifNuoviImmobili
        )
        return ResponseEntity.ok().build()
    }
}
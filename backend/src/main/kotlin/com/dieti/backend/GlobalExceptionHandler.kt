package com.dieti.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.multipart.MaxUploadSizeExceededException

@ControllerAdvice
class GlobalExceptionHandler {

    // Cattura errore dimensione file eccessiva
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxSizeException(exc: MaxUploadSizeExceededException): ResponseEntity<String> {
        println("GlobalExceptionHandler: ERRORE UPLOAD - File troppo grandi!")
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE) // Ritorna 413
            .body("Le immagini inviate superano il limite massimo del server.")
    }

    // Cattura tutti gli altri crash (Database, NullPointer, ecc.)
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(exc: Exception): ResponseEntity<String> {
        println("!!! CRASH SERVER (500) - STACK TRACE COMPLETO: !!!")
        exc.printStackTrace() // Questo stamperà l'errore VERO nella console di IntelliJ
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Errore interno del server: ${exc.message}")
    }
}
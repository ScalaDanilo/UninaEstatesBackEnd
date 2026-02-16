package com.dieti.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.multipart.MaxUploadSizeExceededException
import java.io.PrintWriter
import java.io.StringWriter

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxSizeException(exc: MaxUploadSizeExceededException): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body("Le immagini inviate superano il limite massimo del server.")
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(exc: Exception): ResponseEntity<String> {
        println("!!! CRASH SERVER (500) DETTAGLIATO !!!")
        exc.printStackTrace()

        val sw = StringWriter()
        exc.printStackTrace(PrintWriter(sw))

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Errore interno del server:\n${exc.message}\n\nStack:\n$sw")
    }
}
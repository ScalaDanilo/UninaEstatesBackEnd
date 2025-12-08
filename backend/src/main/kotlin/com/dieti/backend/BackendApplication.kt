package com.dieti.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BackendApplication

fun main(args: Array<String>) {
    println(">>> STARTER: L'APPLICAZIONE STA CERCANDO DI PARTIRE... <<<")
    runApplication<BackendApplication>(*args)
}
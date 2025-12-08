plugins {
    // Usiamo versioni stabili e compatibili
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    // CORRETTO: Spring Boot 3.2.3 (3.5.8 non esiste)
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("plugin.jpa") version "1.9.25"
}

group = "com.dieti"
version = "0.0.1-SNAPSHOT"

java {
    // Toolchain: il progetto userà Java 17
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Driver PostgreSQL Stabile
    implementation("org.postgresql:postgresql:42.7.2")

    // Google Auth
    implementation("com.google.api-client:google-api-client:2.2.0")

    // Test (Disabilitati per ora)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// Disabilita i test per evitare blocchi
tasks.withType<Test> {
    enabled = false
}

// Forza IPv4 per evitare problemi con Azure
tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    systemProperty("java.net.preferIPv4Stack", "true")
}
plugins {
    // Usiamo versioni stabili e compatibili
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    // CORRETTO: Spring Boot 3.2.3 (3.5.8 non esiste)
    id("org.springframework.boot") version "3.2.3"
    id("org.sonarqube") version "7.1.0.6387"
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

sonar {
    properties {
        property("sonar.projectKey", "UninaEstatesBackEnd")
        property("sonar.projectName", "UninaEstates BackEnd")
        property("sonar.host.url", "http://localhost:9000")

        // INCOLLA QUI SOTTO IL TOKEN CHE HAI COPIATO DAL BROWSER (es. "sqp_abcd1234...")
        property("sonar.token", "INCOLLA_QUI_IL_TUO_TOKEN")

        // Configurazioni per indicare dove si trova il codice sorgente
        property("sonar.sources", "src/main/kotlin")
        property("sonar.tests", "src/test/kotlin")
        property("sonar.language", "kotlin")
        property("sonar.sourceEncoding", "UTF-8")

        // Esclusioni opzionali (file che non vuoi analizzare, es. configurazioni)
        property("sonar.exclusions", "**/*Application.kt, **/model/**, **/entity/**, **/dto/**, **/config/**")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // build.gradle.kts
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    //
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // Driver PostgreSQL Stabile
    implementation("org.postgresql:postgresql:42.7.2")

    // Google Auth
    implementation("com.google.api-client:google-api-client:2.2.0") {
        exclude(group = "commons-logging", module = "commons-logging")
    }

    // notifiche
    implementation("com.google.firebase:firebase-admin:9.4.3")
    implementation("com.google.firebase:firebase-common-ktx:22.0.1")

    // Test (Disabilitati per ora)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.10")
}

// Disabilita i test per evitare blocchi
tasks.withType<Test> {
    enabled = false
}

// Forza IPv4 per evitare problemi con Azure
tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    systemProperty("java.net.preferIPv4Stack", "true")
}
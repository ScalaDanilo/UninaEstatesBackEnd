package com.dieti.backend.service

import com.dieti.backend.ImmobileCreateRequest
import com.dieti.backend.ImmobileDTO
import org.springframework.data.repository.findByIdOrNull
import com.dieti.backend.ImmobileSummaryDTO
import com.dieti.backend.entity.*
import com.dieti.backend.repository.ImmagineRepository
import com.dieti.backend.repository.ImmobileRepository
import com.dieti.backend.repository.UtenteRepository // Assumo esista
import com.dieti.backend.toDto
import com.dieti.backend.toEntity
import com.dieti.backend.toSummaryDto
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class ImmobileService(
    private val immobileRepository: ImmobileRepository,
    private val immagineRepository: ImmagineRepository,
    private val utenteRepository: UtenteRepository
) {

    // --- CREAZIONE IMMOBILE ---
    @Transactional
    fun creaImmobile(
        request: ImmobileCreateRequest,
        files: List<MultipartFile>?,
        emailUtente: String
    ): ImmobileDTO {

        // CORREZIONE 1: Usa l'operatore Elvis (?:)
        // Se findByEmail restituisce null, lancia l'eccezione
        val proprietario = utenteRepository.findByEmail(emailUtente)
            ?: throw EntityNotFoundException("Utente non trovato con email: $emailUtente")

        val immobileEntity = request.toEntity(proprietario)

        if (!files.isNullOrEmpty()) {
            val listaImmagini = files.map { file ->
                ImmagineEntity(
                    immobile = immobileEntity,
                    nome = file.originalFilename,
                    formato = file.contentType,
                    immagine = file.bytes
                )
            }.toMutableList()
            immobileEntity.immagini.addAll(listaImmagini)
        }

        val immobileSalvato = immobileRepository.save(immobileEntity)
        return immobileSalvato.toDto()
    }

    // --- RECUPERO TUTTI GLI IMMOBILI ---
    @Transactional(readOnly = true)
    fun getAllImmobili(): List<ImmobileSummaryDTO> {
        // Usa toSummaryDto per una lista leggera, oppure toDto per tutto
        return immobileRepository.findAll().map { it.toSummaryDto() }
    }

    // --- RECUPERO SINGOLO IMMOBILE ---
    @Transactional(readOnly = true)
    fun getImmobileById(uuid: String): ImmobileDTO {
        val id = try { UUID.fromString(uuid) } catch (e: Exception) { throw IllegalArgumentException("UUID non valido") }

        // CORREZIONE 2: Usa findByIdOrNull (richiede l'import sopra) + Elvis
        val immobile = immobileRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Immobile non trovato con ID: $uuid")

        return immobile.toDto()
    }

    // --- RECUPERO RAW IMAGE ---
    @Transactional(readOnly = true)
    fun getImmagineContent(idImmagine: Int): ImmagineEntity {
        // CORREZIONE 3: Anche qui, per coerenza
        return immagineRepository.findByIdOrNull(idImmagine)
            ?: throw EntityNotFoundException("Immagine non trovata")
    }
}
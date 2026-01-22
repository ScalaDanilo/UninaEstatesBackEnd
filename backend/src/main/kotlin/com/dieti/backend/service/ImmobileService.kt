package com.dieti.backend.service

import com.dieti.backend.dto.*
import com.dieti.backend.entity.*
import com.dieti.backend.repository.ImmobileRepository
import com.dieti.backend.repository.UtenteRepository
import com.dieti.backend.repository.ImmagineRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID

@Service
class ImmobileService(
    private val immobileRepository: ImmobileRepository,
    private val utenteRepository: UtenteRepository,
    private val immagineRepository: ImmagineRepository
) {

    @Transactional
    fun creaImmobile(
        request: ImmobileCreateRequest,
        files: List<MultipartFile>?,
        emailUtente: String
    ): ImmobileDTO {

        // 1. Recupera l'utente
        val proprietario = utenteRepository.findByEmail(emailUtente)
            ?: throw EntityNotFoundException("Utente non trovato: $emailUtente")

        // 2. Parsing della data
        var parsedDate: LocalDate? = null
        if (!request.annoCostruzione.isNullOrBlank()) {
            val dataStr = request.annoCostruzione!!
            try {
                parsedDate = LocalDate.parse(dataStr)
            } catch (e: DateTimeParseException) {
                if (dataStr.length == 4 && dataStr.all { it.isDigit() }) {
                    try {
                        parsedDate = LocalDate.of(dataStr.toInt(), 1, 1)
                    } catch (ignore: Exception) {}
                }
            }
        }

        // 3. Crea l'entità Immobile
        val immobileEntity = ImmobileEntity(
            proprietario = proprietario,
            tipoVendita = request.tipoVendita,
            categoria = request.categoria,
            indirizzo = request.indirizzo,
            mq = request.mq,
            piano = request.piano,
            ascensore = request.ascensore,
            arredamento = request.arredamento,
            climatizzazione = request.climatizzazione,
            esposizione = request.esposizione,
            statoProprieta = request.statoProprieta,
            annoCostruzione = parsedDate,
            prezzo = request.prezzo,
            speseCondominiali = request.speseCondominiali,
            descrizione = request.descrizione
        )

        // 4. SALVA PRIMA L'IMMOBILE (Genera UUID)
        val savedImmobile = immobileRepository.save(immobileEntity)

        // 5. Gestione Immagini con CAST ESPLICITO a ByteArray
        if (!files.isNullOrEmpty()) {
            val imageEntities = files.map { file ->

                // CAST / ESTRAZIONE ESPLICITA:
                // MultipartFile -> ByteArray (byte[]) compatibile con 'bytea' di Postgres
                val fileBytes: ByteArray = file.bytes

                ImmagineEntity(
                    immobile = savedImmobile,
                    nome = file.originalFilename,
                    formato = file.contentType,
                    immagine = fileBytes // Passo il ByteArray puro
                )
            }
            immagineRepository.saveAll(imageEntities)
            savedImmobile.immagini.addAll(imageEntities)
        }

        // 6. Gestione Ambienti
        if (request.ambienti.isNotEmpty()) {
            val ambienteEntities = request.ambienti.map { dto ->
                AmbienteEntity(
                    immobile = savedImmobile,
                    tipologia = dto.tipologia,
                    numero = dto.numero
                )
            }
            savedImmobile.ambienti.addAll(ambienteEntities)
            immobileRepository.save(savedImmobile)
        }

        return convertToDto(savedImmobile)
    }

    @Transactional(readOnly = true)
    fun getImmagineContent(id: Int): ImmagineEntity {
        return immagineRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Immagine non trovata") }
    }

    @Transactional(readOnly = true)
    fun getAllImmobili(): List<ImmobileDTO> {
        return immobileRepository.findAll().map { convertToDto(it) }
    }

    @Transactional(readOnly = true)
    fun getImmobileById(id: String): ImmobileDTO {
        val uuid = UUID.fromString(id)
        val entity = immobileRepository.findByIdOrNull(uuid)
            ?: throw EntityNotFoundException("Immobile non trovato")
        return convertToDto(entity)
    }

    private fun convertToDto(entity: ImmobileEntity): ImmobileDTO {
        return ImmobileDTO(
            id = entity.uuid.toString(),
            tipoVendita = entity.tipoVendita,
            categoria = entity.categoria,
            indirizzo = entity.indirizzo,
            prezzo = entity.prezzo,
            mq = entity.mq,
            descrizione = entity.descrizione,
            annoCostruzione = entity.annoCostruzione?.toString(),
            immagini = entity.immagini.map {
                ImmagineDto(it.id ?: 0, "/api/immagini/${it.id}/raw")
            },
            ambienti = entity.ambienti.map {
                AmbienteDto(it.tipologia, it.numero)
            }
        )
    }
}
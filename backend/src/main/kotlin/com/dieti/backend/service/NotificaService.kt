package com.dieti.backend.service

import com.dieti.backend.dto.NotificaDTO
import com.dieti.backend.entity.NotificaEntity
import com.dieti.backend.entity.UtenteRegistratoEntity
import com.dieti.backend.repository.NotificaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class NotificaService(private val notificaRepository: NotificaRepository) {

    @Transactional
    fun inviaNotifica(destinatario: UtenteRegistratoEntity, titolo: String, corpo: String, tipo: String = "SISTEMA") {
        val notifica = NotificaEntity(
            utente = destinatario,
            titolo = titolo,
            corpo = corpo,
            tipo = tipo,
            letto = false
        )
        notificaRepository.save(notifica)
    }

    @Transactional(readOnly = true)
    fun getNotificheUtente(userIdStr: String): List<NotificaDTO> {
        val uuid = UUID.fromString(userIdStr)
        val notifiche = notificaRepository.findByUtenteUuidOrderByDataCreazioneDesc(uuid)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

        // Filtra via le notifiche di tipo TRATTATIVA (perché vanno nell'altra schermata)
        return notifiche
            .filter { it.tipo != "TRATTATIVA" }
            .map { entity ->
                NotificaDTO(
                    id = entity.uuid.toString(),
                    titolo = entity.titolo,
                    corpo = entity.corpo ?: "",
                    data = entity.dataCreazione.format(formatter),
                    letto = entity.letto
                )
            }
    }
}
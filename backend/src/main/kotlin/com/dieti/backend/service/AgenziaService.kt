package com.dieti.backend.service

import com.dieti.backend.dto.AgenziaDTO
import com.dieti.backend.dto.AgenziaOptionDTO
import com.dieti.backend.dto.CreateAgenziaRequest
import com.dieti.backend.entity.AgenziaEntity
import com.dieti.backend.repository.AgenziaRepository
import com.dieti.backend.repository.AmministratoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AgenziaService(
    private val agenziaRepository: AgenziaRepository,
    private val amministratoreRepository: AmministratoreRepository,
    private val geocodingService: GeocodingService
) {

    @Transactional
    fun creaAgenzia(request: CreateAgenziaRequest): AgenziaDTO {
        val adminId = UUID.fromString(request.adminId)

        val admin = amministratoreRepository.findById(adminId)
            .orElseThrow { RuntimeException("Admin non trovato") }

        val (lat, lon) = geocodingService.getCoordinates(request.indirizzo)

        val agenzia = AgenziaEntity(
            nome = request.nome,
            indirizzo = request.indirizzo,
            lat = lat,
            long = lon,
            amministratore = admin
        )

        val saved = agenziaRepository.save(agenzia)

        // FIX: Restituiamo il DTO con l'ID dell'admin
        return AgenziaDTO(
            id = saved.uuid.toString(),
            nome = saved.nome,
            indirizzo = saved.indirizzo,
            lat = saved.lat,
            long = saved.long,
            adminId = saved.amministratore?.uuid?.toString()
        )
    }

    @Transactional(readOnly = true)
    fun getAgenzieOptionsForAdmin(): List<AgenziaOptionDTO> {
        return agenziaRepository.findAll().map { agenzia ->
            val haCapo = agenzia.agenti.any { it.isCapo }
            AgenziaOptionDTO(
                id = agenzia.uuid.toString(),
                nome = agenzia.nome,
                haCapo = haCapo
            )
        }
    }
}
package com.dieti.backend

import com.dieti.backend.dto.ImmobileDTO
import com.dieti.backend.dto.ImmobileSearchFilters
import com.dieti.backend.dto.toDto
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

import com.dieti.backend.entity.ImmobileEntity
import com.dieti.backend.repository.ImmobileRepository
import com.dieti.backend.repository.UtenteRepository
import com.dieti.backend.service.ImmobileService
import com.dieti.backend.service.RedisGeoService
import com.dieti.backend.service.UltimaRicercaService
import io.mockk.*
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.springframework.data.jpa.domain.Specification
import java.util.UUID

import com.dieti.backend.dto.ChangeMyPasswordRequest
import com.dieti.backend.dto.ImmobileCreateRequest
import com.dieti.backend.entity.AmministratoreEntity
import com.dieti.backend.entity.ImmagineEntity
import com.dieti.backend.entity.UtenteRegistratoEntity
import com.dieti.backend.repository.AmministratoreRepository
import com.dieti.backend.repository.ImmagineRepository
import com.dieti.backend.service.AmministratoreService
import com.dieti.backend.service.GeoapifyService
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.multipart.MultipartFile

@SpringBootTest // <-- COMMENTATO PER EVITARE CHE IL TEST BLOCCHI LA BUILD
class BackendApplicationTests {

    // Mock delle dipendenze
    private val redisGeoService: RedisGeoService = mockk()
    private val ultimaRicercaService: UltimaRicercaService = mockk()
    private val utenteRepository: UtenteRepository = mockk()
    private val immagineRepository: ImmagineRepository = mockk()
    private val geoapifyService: GeoapifyService = mockk()





    @BeforeEach
    fun setup() {
        // Resetta i mock prima di ogni test per garantire un ambiente pulito
        clearMocks(immobileRepository, redisGeoService, ultimaRicercaService)
    }


    // Mock del repository (non tocchiamo il database reale)
    private val immobileRepository: ImmobileRepository = mockk()

    // Service sotto test (iniettiamo il mock)
    // Nota: Aggiungi qui altri mock se ImmobileService ha altre dipendenze (es. GeoapifyService)
    // Service sotto test
    private val immobileService = ImmobileService(
        immobileRepository,
        utenteRepository,
        immagineRepository,
        ultimaRicercaService,
        geoapifyService,
        redisGeoService
    )

    @Test
    fun `searchImmobili esegue ricerca GEO e applica filtri in memoria`() {
        // Arrange
        // Filtri: GEO + Prezzo (max 2000)
        val filters = ImmobileSearchFilters(
            lat = 40.85, lon = 14.26, radiusKm = 5.0,
            maxPrezzo = 2000,
            query = null // Query null per evitare salvataggio cronologia in questo test
        )

        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()

        // Immobile 1: Corrisponde ai filtri (Prezzo 1500 <= 2000)
        val entityValid = mockk<ImmobileEntity>(relaxed = true)
        every { entityValid.uuid } returns uuid1
        every { entityValid.prezzo } returns 1500
        every { entityValid.mq } returns 100 // Valore dummy
        every { entityValid.toDto() } returns mockk<ImmobileDTO>()

        // Immobile 2: NON corrisponde ai filtri (Prezzo 2500 > 2000)
        val entityInvalid = mockk<ImmobileEntity>(relaxed = true)
        every { entityInvalid.uuid } returns uuid2
        every { entityInvalid.prezzo } returns 2500
        // Nota: non serve mockare toDto per entityInvalid perché dovrebbe essere filtrata via

        // Simuliamo Redis che trova 2 ID vicini
        every { redisGeoService.findNearbyImmobiliIds(40.85, 14.26, 5.0) } returns listOf(uuid1.toString(), uuid2.toString())

        // Simuliamo il recupero dal DB degli immobili trovati da Redis
        every { immobileRepository.findAllById(listOf(uuid1, uuid2)) } returns listOf(entityValid, entityInvalid)

        // Act
        val result = immobileService.searchImmobili(filters, null)

        // Assert
        assertEquals(1, result.size, "Dovrebbe rimanere solo l'immobile che rispetta il filtro prezzo")
        // Verifica che sia stato usato il flusso Geo
        verify(exactly = 1) { redisGeoService.findNearbyImmobiliIds(any(), any(), any()) }
        // Verifica che NON sia stata usata la Specification
        verify(exactly = 0) { immobileRepository.findAll(any<Specification<ImmobileEntity>>()) }
    }


    // Mock delle dipendenze
    private val amministratoreRepository: AmministratoreRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()

    // Service sotto test
    private val amministratoreService =
        AmministratoreService(amministratoreRepository, passwordEncoder)

    @Test
    fun `cambiaPasswordPersonale lancia eccezione se password attuale non corretta`() {
        // Arrange
        val email = "admin@test.com"
        val oldPasswordErrata = "passwordSbagliata"
        val oldPasswordHashataNelDb = "hashDellaPasswordVera"
        val request = ChangeMyPasswordRequest(oldPassword = oldPasswordErrata, newPassword = "newPass")

        val adminMock = mockk<AmministratoreEntity>(relaxed = true)
        every { adminMock.password } returns oldPasswordHashataNelDb

        // Simuliamo che l'admin venga trovato
        every { amministratoreRepository.findByEmail(email) } returns adminMock

        // Simuliamo che il controllo della password fallisca (false)
        every { passwordEncoder.matches(oldPasswordErrata, oldPasswordHashataNelDb) } returns false

        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) {
            amministratoreService.cambiaPasswordPersonale(email, request)
        }

        assertEquals("Password attuale non corretta", exception.message)

        // Verifichiamo che il metodo save NON sia stato chiamato (la password non deve cambiare)
        verify(exactly = 0) { amministratoreRepository.save(any()) }
    }

    @Test
    fun `creaImmobile salva immobile, coordinate e immagini correttamente`() {
        // Arrange
        val email = "proprietario@test.com"
        val request = mockk<ImmobileCreateRequest>(relaxed = true)
        val fileMock = mockk<MultipartFile>(relaxed = true)
        val files = listOf(fileMock)

        // Mock dati richiesta
        every { request.indirizzo } returns "Via Roma 1"
        every { request.localita } returns "Napoli"
        every { request.annoCostruzione } returns "2020"
        every { request.ambienti } returns emptyList() // Nessun ambiente aggiuntivo per semplicità
        every { fileMock.bytes } returns ByteArray(10)
        every { fileMock.originalFilename } returns "foto.jpg"
        every { fileMock.contentType } returns "image/jpeg"

        // Mock Utente
        val proprietarioMock = mockk<UtenteRegistratoEntity>()
        every { utenteRepository.findByEmail(email) } returns proprietarioMock

        // Mock Geoapify (Coordinate e Amenities)
        val coordsMock: GeoapifyService.GeoResult = mockk()
        every { coordsMock.lat } returns 40.85
        every { coordsMock.lon } returns 14.26
        every { coordsMock.city } returns "Napoli"
        every { geoapifyService.getCoordinates(any(), any()) } returns coordsMock

        val amenitiesMock: GeoapifyService.AmenitiesResult = mockk()
        every { amenitiesMock.hasParks } returns true
        every { geoapifyService.checkAmenities(any(), any()) } returns amenitiesMock

        // Mock Salvataggio Immobile
        val savedImmobile = mockk<ImmobileEntity>(relaxed = true)
        every { savedImmobile.uuid } returns UUID.randomUUID()
        every { savedImmobile.immagini } returns mutableListOf()
        every { savedImmobile.ambienti } returns mutableListOf()
        every { savedImmobile.toDto() } returns mockk<ImmobileDTO>()

        // Il metodo save viene chiamato. Usiamo `atLeast = 1` perché nel codice fornito
        // potrebbero esserci chiamate multiple (save iniziale + save post-immagini/ambienti).
        every { immobileRepository.save(any<ImmobileEntity>()) } returns savedImmobile

        // Mock Redis e Immagini
        every { redisGeoService.addLocation(any(), any(), any()) } just Runs
        every { redisGeoService.addCity(any()) } just Runs
        every { immagineRepository.saveAll(any<List<ImmagineEntity>>()) } returns emptyList()

        // Act
        immobileService.creaImmobile(request, files, email)

        // Assert
        verify(exactly = 1) { utenteRepository.findByEmail(email) }
        verify(exactly = 1) { geoapifyService.getCoordinates("Via Roma 1", "Napoli") }

        // Verifica salvataggio nel DB
        verify(atLeast = 1) { immobileRepository.save(any<ImmobileEntity>()) }

        // Verifica integrazione Redis
        verify(exactly = 1) { redisGeoService.addLocation(savedImmobile.uuid.toString(), 40.85, 14.26) }
        verify(exactly = 1) { redisGeoService.addCity("Napoli") }

        // Verifica salvataggio immagini
        verify(exactly = 1) { immagineRepository.saveAll(any<List<ImmagineEntity>>()) }
    }
}
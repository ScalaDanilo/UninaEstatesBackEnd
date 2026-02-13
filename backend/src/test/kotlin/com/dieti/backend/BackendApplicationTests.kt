package com.dieti.backend

import com.dieti.backend.dto.ChangeMyPasswordRequest
import com.dieti.backend.dto.ImmobileCreateRequest
import com.dieti.backend.dto.ImmobileDTO
import com.dieti.backend.dto.toDto
import com.dieti.backend.entity.AgenteEntity
import com.dieti.backend.entity.AgenziaEntity
import com.dieti.backend.entity.AmministratoreEntity
import com.dieti.backend.entity.ImmagineEntity
import com.dieti.backend.entity.ImmobileEntity
import com.dieti.backend.entity.UtenteRegistratoEntity
import com.dieti.backend.repository.*
import com.dieti.backend.service.AmministratoreService
import com.dieti.backend.service.GeocodingService
import com.dieti.backend.service.GestioneImmobiliService
import com.dieti.backend.service.ImmobileService
import com.dieti.backend.service.NotificaService
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

/**
 * BackendApplicationTests
 * Contiene Unit Test per i servizi principali:
 * - ImmobileService (Creazione e Cancellazione - Happy Path)
 * - AmministratoreService (Cambio Password - Failure Path)
 * - GestioneImmobiliService (Recupero Pendenti - Happy Path)
 */
class BackendApplicationTests {

    // --- DIPENDENZE CONDIVISE ---
    private val immobileRepository: ImmobileRepository = mockk()
    private val utenteRepository: UtenteRepository = mockk()
    private val agenteRepository: AgenteRepository = mockk()
    private val agenziaRepository: AgenziaRepository = mockk()
    private val immagineRepository: ImmagineRepository = mockk()
    private val ambienteRepository: AmbienteRepository = mockk()
    private val geocodingService: GeocodingService = mockk()

    // Dipendenze specifiche GestioneImmobili
    private val notificaService: NotificaService = mockk()

    // Dipendenze specifiche Amministratore
    private val amministratoreRepository: AmministratoreRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()

    // --- SERVICES SOTTO TEST ---
    private lateinit var immobileService: ImmobileService
    private lateinit var amministratoreService: AmministratoreService
    private lateinit var gestioneImmobiliService: GestioneImmobiliService

    @BeforeEach
    fun setup() {
        // Reset dei mock
        clearMocks(
            immobileRepository, utenteRepository, agenteRepository, agenziaRepository,
            immagineRepository, ambienteRepository, geocodingService,
            amministratoreRepository, passwordEncoder, notificaService
        )

        // Inizializzazione Services
        immobileService = ImmobileService(
            immobileRepository, utenteRepository, agenteRepository, agenziaRepository,
            immagineRepository, ambienteRepository, geocodingService
        )

        amministratoreService = AmministratoreService(
            amministratoreRepository, passwordEncoder
        )

        gestioneImmobiliService = GestioneImmobiliService(
            immobileRepository, agenteRepository, notificaService
        )
    }

    // ==========================================
    // TEST 1: Creazione Immobile (Successo)
    // ==========================================
    @Test
    fun `creaImmobile successo (Agente) - Geocoding attivo e assegnazione agenzia`() {
        // Test Case ID: TC_IMM_CREA_01

        // Arrange
        val userId = UUID.randomUUID().toString()
        val request = mockk<ImmobileCreateRequest>(relaxed = true)
        val fileMock = mockk<MultipartFile>(relaxed = true)
        val files = listOf(fileMock)

        // Dati richiesta: Indirizzo presente, Coordinate assenti (Triggera Geocoding)
        every { request.indirizzo } returns "Via Roma 1"
        every { request.localita } returns "Napoli"
        every { request.lat } returns null
        every { request.long } returns null
        every { request.ambienti } returns emptyList()
        every { fileMock.bytes } returns ByteArray(10)
        every { fileMock.originalFilename } returns "foto.jpg"

        // Mock Ruoli: Utente è Agente
        val utenteMock = mockk<UtenteRegistratoEntity>()
        val agenteMock = mockk<AgenteEntity>()
        val agenziaMock = mockk<AgenziaEntity>()
        every { agenziaMock.nome } returns "Agenzia Alpha"
        every { agenteMock.agenzia } returns agenziaMock

        every { utenteRepository.findById(UUID.fromString(userId)) } returns Optional.of(utenteMock)
        every { agenteRepository.findById(UUID.fromString(userId)) } returns Optional.of(agenteMock)

        // Mock Geocoding (Successo)
        val coords = GeocodingService.GeoResult(40.85, 14.26, "Napoli")
        every { geocodingService.getCoordinates("Via Roma 1", "Napoli") } returns coords

        // Mock Salvataggio
        val savedImmobile = mockk<ImmobileEntity>(relaxed = true)
        val slotImmobile = slot<ImmobileEntity>()
        every { savedImmobile.uuid } returns UUID.randomUUID()
        every { savedImmobile.toDto() } returns mockk<ImmobileDTO>()

        // Capture entity per assert
        every { immobileRepository.save(capture(slotImmobile)) } returns savedImmobile
        every { immagineRepository.saveAll(any<List<ImmagineEntity>>()) } returns emptyList()

        // Act
        immobileService.creaImmobile(request, files, userId)

        // Assert
        val capturedEntity = slotImmobile.captured

        // Verifica Geocoding
        assertEquals(40.85, capturedEntity.lat)
        assertEquals(14.26, capturedEntity.long)

        // Verifica Assegnazione Agenzia (Logica Agente)
        assertEquals(agenziaMock, capturedEntity.agenzia)
        assertEquals(agenteMock, capturedEntity.agente)

        // Verifica persistenza
        verify(exactly = 1) { immobileRepository.save(any<ImmobileEntity>()) }
        verify(exactly = 1) { immagineRepository.saveAll(any<List<ImmagineEntity>>()) }
    }

    // ==========================================
    // TEST 2: Cancellazione Immobile (Successo)
    // ==========================================
    @Test
    fun `cancellaImmobile successo - Eliminazione corretta immobile esistente`() {
        // Test Case ID: TC_IMM_DEL_01

        // Arrange
        val immobileUuid = UUID.randomUUID()
        val userId = UUID.randomUUID().toString()
        val immobileEntity = mockk<ImmobileEntity>(relaxed = true)

        // Simuliamo che l'immobile esista nel DB
        every { immobileRepository.findById(immobileUuid) } returns Optional.of(immobileEntity)

        // Simuliamo la cancellazione (void)
        every { immobileRepository.delete(immobileEntity) } just Runs

        // Act
        immobileService.cancellaImmobile(immobileUuid.toString(), userId)

        // Assert
        verify(exactly = 1) { immobileRepository.findById(immobileUuid) }
        verify(exactly = 1) { immobileRepository.delete(immobileEntity) }
    }

    // ==========================================
    // TEST 3: Cambio Password Admin (Fallimento)
    // ==========================================
    @Test
    fun `cambiaPasswordPersonale fallimento - Password attuale errata`() {
        // Test Case ID: TC_ADMIN_PWD_FAIL

        // Arrange
        val email = "admin@test.com"
        val request = ChangeMyPasswordRequest(
            oldPassword = "wrongPassword123",
            newPassword = "newSecurePassword!"
        )
        val encodedRealPassword = "encodedRealPasswordHash"

        val adminEntity = mockk<AmministratoreEntity>(relaxed = true)
        every { adminEntity.password } returns encodedRealPassword

        // 1. L'admin viene trovato
        every { amministratoreRepository.findByEmail(email) } returns adminEntity

        // 2. Il match della password fallisce (oldPassword vs realPassword)
        every { passwordEncoder.matches(request.oldPassword, encodedRealPassword) } returns false

        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) {
            amministratoreService.cambiaPasswordPersonale(email, request)
        }

        // Verifica messaggio errore specifico
        assertEquals("Password attuale non corretta", exception.message)

        // Verifica che il salvataggio NON avvenga
        verify(exactly = 0) { amministratoreRepository.save(any()) }
    }

    // ==========================================
    // TEST 4: Gestione Immobili - Recupero Pendenti (Successo)
    // ==========================================
    @Test
    fun `getImmobiliDaApprovare successo - Recupera lista filtrata per agenzia del manager`() {
        // Test Case ID: TC_GEST_GET_01

        // Arrange
        val managerId = UUID.randomUUID()
        val agenziaId = UUID.randomUUID()
        val managerIdStr = managerId.toString()

        // 1. Mock Manager e Agenzia
        val agenziaMock = mockk<AgenziaEntity>(relaxed = true)
        every { agenziaMock.uuid } returns agenziaId

        val managerMock = mockk<AgenteEntity>(relaxed = true)
        every { managerMock.agenzia } returns agenziaMock
        every { agenteRepository.findById(managerId) } returns Optional.of(managerMock)

        // 2. Mock Immobile Pendente con Immagine
        val immobilePendente = mockk<ImmobileEntity>(relaxed = true)
        val immagineMock = mockk<ImmagineEntity>(relaxed = true)
        every { immagineMock.id } returns 101

        every { immobilePendente.uuid } returns UUID.randomUUID()
        every { immobilePendente.categoria } returns "Appartamento"
        every { immobilePendente.localita } returns "Milano"
        every { immobilePendente.indirizzo } returns "Via Dante"
        every { immobilePendente.annoCostruzione } returns LocalDate.of(2022, 1, 1)
        every { immobilePendente.immagini } returns mutableListOf(immagineMock)

        // 3. Mock Repository Immobili
        every { immobileRepository.findRichiestePendentiPerAgenzia(agenziaId) } returns listOf(immobilePendente)

        // Act
        val result = gestioneImmobiliService.getImmobiliDaApprovare(managerIdStr)

        // Assert
        assertEquals(1, result.size)
        val dto = result[0]
        assertEquals("APPARTAMENTO", dto.titolo)
        assertEquals("Milano - Via Dante", dto.descrizione)
        assertEquals("DA APPROVARE", dto.stato)
        assertEquals("/api/immagini/101/raw", dto.immagineUrl) // Verifica la mappatura dell'URL immagine

        // Verifica che sia stato chiamato il metodo ottimizzato del repository
        verify(exactly = 1) { immobileRepository.findRichiestePendentiPerAgenzia(agenziaId) }
    }
}
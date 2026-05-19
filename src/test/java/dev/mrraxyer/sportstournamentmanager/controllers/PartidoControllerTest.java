package dev.mrraxyer.sportstournamentmanager.controllers;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.models.Partido;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.services.impl.PartidoService;
import dev.mrraxyer.sportstournamentmanager.repositories.TorneoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PartidoController endpoints:
 * - GET /api/partidos/{id}
 * - GET /api/partidos
 * - GET /api/partidos/torneo/{torneoId}
 * - POST /api/partidos
 * - PUT /api/partidos/{id}
 * - PUT /api/partidos/{id}/resultado (main focus: estado guard)
 * - DELETE /api/partidos/{id}
 */
@ExtendWith(MockitoExtension.class)
class PartidoControllerTest {

    @Mock
    private PartidoService partidoService;

    @Mock
    private TorneoRepository torneoRepository;

    @InjectMocks
    private PartidoController partidoController;

    private Torneo torneo;
    private Equipo equipoLocal;
    private Equipo equipoVisitante;
    private Partido partido;

    @BeforeEach
    void setUp() {
        // Initialize test data
        torneo = new Torneo();
        torneo.setTorneosId(1);
        torneo.setNombre("Copa Nacional 2024");
        torneo.setEstado("ACTIVO");

        equipoLocal = new Equipo();
        equipoLocal.setEquiposId(10);
        equipoLocal.setNombre("Equipo A");
        equipoLocal.setTorneo(torneo);

        equipoVisitante = new Equipo();
        equipoVisitante.setEquiposId(11);
        equipoVisitante.setNombre("Equipo B");
        equipoVisitante.setTorneo(torneo);

        partido = new Partido();
        partido.setPartidosId(1);
        partido.setTorneo(torneo);
        partido.setEquipoLocal(equipoLocal);
        partido.setEquipoVisitante(equipoVisitante);
        partido.setGolesLocal(0);
        partido.setGolesVisitante(0);
        partido.setJugado(false);
        partido.setFechaPartido(LocalDateTime.of(2024, 6, 15, 15, 0));
        partido.setGrupo("A");
    }

    // ========== GET /api/partidos/{id} Tests ==========

    /**
     * Test 1: GET /partidos/{id} - Success
     * Verify that when a partido exists, returns 200 OK with partido data
     */
    @Test
    void testObtenerPartidoSuccess() {
        // Arrange
        when(partidoService.findById(1)).thenReturn(Optional.of(partido));

        // Act
        ResponseEntity<ApiResponse<Partido>> response =
                partidoController.obtenerPartido(1);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getPartidosId());
        assertEquals("Equipo A", response.getBody().getData().getEquipoLocal().getNombre());

        verify(partidoService, times(1)).findById(1);
    }

    /**
     * Test 2: GET /partidos/{id} - Not found
     * Verify that when partido doesn't exist, returns 404 Not Found
     */
    @Test
    void testObtenerPartidoNotFound() {
        // Arrange
        when(partidoService.findById(999)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<Partido>> response =
                partidoController.obtenerPartido(999);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertNull(response.getBody().getData());

        verify(partidoService, times(1)).findById(999);
    }

    // ========== GET /api/partidos Tests ==========

    /**
     * Test 3: GET /partidos - List all matches
     * Verify that listing all partidos returns 200 OK with partido list
     */
    @Test
    void testListarPartidosSuccess() {
        // Arrange
        Partido partido2 = new Partido();
        partido2.setPartidosId(2);
        partido2.setTorneo(torneo);
        partido2.setEquipoLocal(equipoLocal);
        partido2.setEquipoVisitante(equipoVisitante);

        List<Partido> partidos = new ArrayList<>();
        partidos.add(partido);
        partidos.add(partido2);

        when(partidoService.findAll()).thenReturn(partidos);

        // Act
        ResponseEntity<ApiResponse<List<Partido>>> response =
                partidoController.listarPartidos();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertNotNull(response.getBody().getData());
        assertEquals(2, response.getBody().getData().size());

        verify(partidoService, times(1)).findAll();
    }

    // ========== GET /api/partidos/torneo/{torneoId} Tests ==========

    /**
     * Test 4: GET /partidos/torneo/{torneoId} - Success with partidos
     * Verify that getting partidos by tournament returns 200 OK with list
     */
    @Test
    void testListarPartidosPorTorneoSuccess() {
        // Arrange
        Partido partido2 = new Partido();
        partido2.setPartidosId(2);
        partido2.setTorneo(torneo);
        partido2.setEquipoLocal(equipoLocal);
        partido2.setEquipoVisitante(equipoVisitante);

        List<Partido> partidos = new ArrayList<>();
        partidos.add(partido);
        partidos.add(partido2);

        when(torneoRepository.findById(1)).thenReturn(Optional.of(torneo));
        when(partidoService.findByTorneo(torneo)).thenReturn(partidos);

        // Act
        ResponseEntity<ApiResponse<List<Partido>>> response =
                partidoController.listarPartidosPorTorneo(1);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertNotNull(response.getBody().getData());
        assertEquals(2, response.getBody().getData().size());

        verify(torneoRepository, times(1)).findById(1);
        verify(partidoService, times(1)).findByTorneo(torneo);
    }

    /**
     * Test 5: GET /partidos/torneo/{torneoId} - Torneo not found
     * Verify that 404 is returned when torneo doesn't exist
     */
    @Test
    void testListarPartidosPorTorneoNotFound() {
        // Arrange
        when(torneoRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<List<Partido>>> response =
                partidoController.listarPartidosPorTorneo(999);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertNull(response.getBody().getData());

        verify(torneoRepository, times(1)).findById(999);
    }

    // ========== POST /api/partidos Tests ==========

    /**
     * Test 6: POST /partidos - Create new partido
     * Verify that creating a new partido returns 201 Created
     */
    @Test
    void testCrearPartidoSuccess() {
        // Arrange
        Partido nuevoPartido = new Partido();
        nuevoPartido.setPartidosId(null);
        nuevoPartido.setTorneo(torneo);
        nuevoPartido.setEquipoLocal(equipoLocal);
        nuevoPartido.setEquipoVisitante(equipoVisitante);
        nuevoPartido.setGolesLocal(null);
        nuevoPartido.setGolesVisitante(null);
        nuevoPartido.setFechaPartido(LocalDateTime.of(2024, 6, 20, 18, 0));

        Partido partidoGuardado = new Partido();
        partidoGuardado.setPartidosId(100);
        partidoGuardado.setTorneo(torneo);
        partidoGuardado.setEquipoLocal(equipoLocal);
        partidoGuardado.setEquipoVisitante(equipoVisitante);
        partidoGuardado.setGolesLocal(0);
        partidoGuardado.setGolesVisitante(0);
        partidoGuardado.setJugado(false);
        partidoGuardado.setFechaPartido(LocalDateTime.of(2024, 6, 20, 18, 0));

        when(partidoService.save(any(Partido.class))).thenReturn(partidoGuardado);

        // Act
        ResponseEntity<ApiResponse<Partido>> response =
                partidoController.crearPartido(nuevoPartido);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(201, response.getBody().getStatus());
        assertNotNull(response.getBody().getData());
        assertEquals(100, response.getBody().getData().getPartidosId());
        assertEquals(0, response.getBody().getData().getGolesLocal());
        assertEquals(0, response.getBody().getData().getGolesVisitante());

        verify(partidoService, times(1)).save(any(Partido.class));
    }

    // ========== PUT /api/partidos/{id} Tests ==========

    /**
     * Test 7: PUT /partidos/{id} - Update partido
     * Verify that updating a partido returns 200 OK with updated data
     */
    @Test
    void testActualizarPartidoSuccess() {
        // Arrange
        Partido partidoActualizado = new Partido();
        partidoActualizado.setGolesLocal(2);
        partidoActualizado.setGolesVisitante(1);

        Partido partidoGuardado = new Partido();
        partidoGuardado.setPartidosId(1);
        partidoGuardado.setTorneo(torneo);
        partidoGuardado.setEquipoLocal(equipoLocal);
        partidoGuardado.setEquipoVisitante(equipoVisitante);
        partidoGuardado.setGolesLocal(2);
        partidoGuardado.setGolesVisitante(1);
        partidoGuardado.setJugado(false);
        partidoGuardado.setFechaPartido(LocalDateTime.of(2024, 6, 15, 15, 0));

        when(partidoService.findById(1)).thenReturn(Optional.of(partido));
        when(partidoService.save(any(Partido.class))).thenReturn(partidoGuardado);

        // Act
        ResponseEntity<ApiResponse<Partido>> response =
                partidoController.actualizarPartido(1, partidoActualizado);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertNotNull(response.getBody().getData());
        assertEquals(2, response.getBody().getData().getGolesLocal());
        assertEquals(1, response.getBody().getData().getGolesVisitante());

        verify(partidoService, times(1)).findById(1);
        verify(partidoService, times(1)).save(any(Partido.class));
    }

    /**
     * Test 8: PUT /partidos/{id} - Partido not found
     * Verify that updating non-existent partido returns 404 Not Found
     */
    @Test
    void testActualizarPartidoNotFound() {
        // Arrange
        Partido partidoActualizado = new Partido();
        partidoActualizado.setGolesLocal(2);
        when(partidoService.findById(999)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<Partido>> response =
                partidoController.actualizarPartido(999, partidoActualizado);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertNull(response.getBody().getData());

        verify(partidoService, times(1)).findById(999);
    }

    // ========== PUT /api/partidos/{id}/resultado Tests (MAIN FOCUS) ==========

    /**
     * Test 9: PUT /partidos/{id}/resultado - Success with torneo ACTIVO
     * Verify that registering resultado when torneo is ACTIVO succeeds (200 OK)
     */
    @Test
    void testRegistrarResultadoSuccessWhenTorneoActivo() {
        // Arrange
        torneo.setEstado("ACTIVO");
        partido.setTorneo(torneo);

        Partido partidoGuardado = new Partido();
        partidoGuardado.setPartidosId(1);
        partidoGuardado.setTorneo(torneo);
        partidoGuardado.setEquipoLocal(equipoLocal);
        partidoGuardado.setEquipoVisitante(equipoVisitante);
        partidoGuardado.setGolesLocal(3);
        partidoGuardado.setGolesVisitante(2);
        partidoGuardado.setJugado(true);
        partidoGuardado.setFechaPartido(LocalDateTime.of(2024, 6, 15, 15, 0));
        partidoGuardado.setGrupo("A");

        when(partidoService.findById(1)).thenReturn(Optional.of(partido));
        when(partidoService.save(any(Partido.class))).thenReturn(partidoGuardado);

        // Act
        ResponseEntity<ApiResponse<Partido>> response =
                partidoController.registrarResultado(1, 3, 2);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertNotNull(response.getBody().getData());
        assertEquals(3, response.getBody().getData().getGolesLocal());
        assertEquals(2, response.getBody().getData().getGolesVisitante());
        assertTrue(response.getBody().getData().getJugado());
        assertTrue(response.getBody().getMessage().contains("Resultado registrado")); // Success message

        verify(partidoService, times(1)).findById(1);
        verify(partidoService, times(1)).save(any(Partido.class));
    }

    /**
     * Test 10: PUT /partidos/{id}/resultado - REJECTED when torneo is BORRADOR
     * Verify that registering resultado when torneo is BORRADOR returns 409 Conflict
     * This is the main guard test for estado validation
     */
    @Test
    void testRegistrarResultadoRejectedWhenTorneoBorrador() {
        // Arrange
        Torneo torneoBorrador = new Torneo();
        torneoBorrador.setTorneosId(1);
        torneoBorrador.setNombre("Copa Nacional 2024");
        torneoBorrador.setEstado("BORRADOR");

        Partido partidoBorrador = new Partido();
        partidoBorrador.setPartidosId(1);
        partidoBorrador.setTorneo(torneoBorrador);
        partidoBorrador.setEquipoLocal(equipoLocal);
        partidoBorrador.setEquipoVisitante(equipoVisitante);
        partidoBorrador.setGolesLocal(0);
        partidoBorrador.setGolesVisitante(0);
        partidoBorrador.setJugado(false);
        partidoBorrador.setFechaPartido(LocalDateTime.of(2024, 6, 15, 15, 0));

        when(partidoService.findById(1)).thenReturn(Optional.of(partidoBorrador));

        // Act
        ResponseEntity<ApiResponse<Partido>> response =
                partidoController.registrarResultado(1, 2, 1);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getMessage().contains("ACTIVO"));

        // Service should NOT be called to save
        verify(partidoService, times(1)).findById(1);
        verify(partidoService, never()).save(any(Partido.class));
    }

    /**
     * Test 11: PUT /partidos/{id}/resultado - REJECTED when torneo is FINALIZADO
     * Verify that registering resultado when torneo is FINALIZADO returns 409 Conflict
     */
    @Test
    void testRegistrarResultadoRejectedWhenTorneoFinalizado() {
        // Arrange
        Torneo torneoFinalizado = new Torneo();
        torneoFinalizado.setTorneosId(1);
        torneoFinalizado.setNombre("Copa Nacional 2024");
        torneoFinalizado.setEstado("FINALIZADO");

        Partido partidoFinalizado = new Partido();
        partidoFinalizado.setPartidosId(1);
        partidoFinalizado.setTorneo(torneoFinalizado);
        partidoFinalizado.setEquipoLocal(equipoLocal);
        partidoFinalizado.setEquipoVisitante(equipoVisitante);
        partidoFinalizado.setGolesLocal(0);
        partidoFinalizado.setGolesVisitante(0);
        partidoFinalizado.setJugado(false);
        partidoFinalizado.setFechaPartido(LocalDateTime.of(2024, 6, 15, 15, 0));

        when(partidoService.findById(1)).thenReturn(Optional.of(partidoFinalizado));

        // Act
        ResponseEntity<ApiResponse<Partido>> response =
                partidoController.registrarResultado(1, 2, 1);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertNull(response.getBody().getData());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getMessage().contains("ACTIVO"));

        // Service should NOT be called to save
        verify(partidoService, times(1)).findById(1);
        verify(partidoService, never()).save(any(Partido.class));
    }

    /**
     * Test 12: PUT /partidos/{id}/resultado - REJECTED when torneo is null
     * Verify that registering resultado when torneo is null returns 409 Conflict
     */
    @Test
    void testRegistrarResultadoRejectedWhenTorneoNull() {
        // Arrange
        Partido partidoSinTorneo = new Partido();
        partidoSinTorneo.setPartidosId(1);
        partidoSinTorneo.setTorneo(null);
        partidoSinTorneo.setEquipoLocal(equipoLocal);
        partidoSinTorneo.setEquipoVisitante(equipoVisitante);

        when(partidoService.findById(1)).thenReturn(Optional.of(partidoSinTorneo));

        // Act
        ResponseEntity<ApiResponse<Partido>> response =
                partidoController.registrarResultado(1, 2, 1);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertNull(response.getBody().getData());

        // Service should NOT be called to save
        verify(partidoService, times(1)).findById(1);
        verify(partidoService, never()).save(any(Partido.class));
    }

    /**
     * Test 13: PUT /partidos/{id}/resultado - Partido not found
     * Verify that registering resultado for non-existent partido returns 404 Not Found
     */
    @Test
    void testRegistrarResultadoPartidoNotFound() {
        // Arrange
        when(partidoService.findById(999)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<Partido>> response =
                partidoController.registrarResultado(999, 2, 1);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertNull(response.getBody().getData());

        verify(partidoService, times(1)).findById(999);
        verify(partidoService, never()).save(any(Partido.class));
    }

    /**
     * Test 14: PUT /partidos/{id}/resultado - Case insensitive estado check
     * Verify that estado comparison is case-insensitive (activo vs ACTIVO)
     */
    @Test
    void testRegistrarResultadoSuccessWithLowercaseEstado() {
        // Arrange
        Torneo torneoActivo = new Torneo();
        torneoActivo.setTorneosId(1);
        torneoActivo.setEstado("activo"); // lowercase

        Partido partidoActivo = new Partido();
        partidoActivo.setPartidosId(1);
        partidoActivo.setTorneo(torneoActivo);
        partidoActivo.setEquipoLocal(equipoLocal);
        partidoActivo.setEquipoVisitante(equipoVisitante);

        Partido partidoGuardado = new Partido();
        partidoGuardado.setPartidosId(1);
        partidoGuardado.setTorneo(torneoActivo);
        partidoGuardado.setEquipoLocal(equipoLocal);
        partidoGuardado.setEquipoVisitante(equipoVisitante);
        partidoGuardado.setGolesLocal(3);
        partidoGuardado.setGolesVisitante(2);
        partidoGuardado.setJugado(true);

        when(partidoService.findById(1)).thenReturn(Optional.of(partidoActivo));
        when(partidoService.save(any(Partido.class))).thenReturn(partidoGuardado);

        // Act
        ResponseEntity<ApiResponse<Partido>> response =
                partidoController.registrarResultado(1, 3, 2);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getStatus());

        verify(partidoService, times(1)).findById(1);
        verify(partidoService, times(1)).save(any(Partido.class));
    }

    // ========== DELETE /api/partidos/{id} Tests ==========

    /**
     * Test 15: DELETE /partidos/{id} - Success
     * Verify that deleting an existing partido returns 204 No Content
     */
    @Test
    void testEliminarPartidoSuccess() {
        // Arrange
        when(partidoService.existsById(1)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = partidoController.eliminarPartido(1);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(partidoService, times(1)).existsById(1);
        verify(partidoService, times(1)).deleteById(1);
    }

    /**
     * Test 16: DELETE /partidos/{id} - Not found
     * Verify that deleting non-existent partido returns 404 Not Found
     */
    @Test
    void testEliminarPartidoNotFound() {
        // Arrange
        when(partidoService.existsById(999)).thenReturn(false);

        // Act
        ResponseEntity<Void> response = partidoController.eliminarPartido(999);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(partidoService, times(1)).existsById(999);
        verify(partidoService, never()).deleteById(any());
    }
}

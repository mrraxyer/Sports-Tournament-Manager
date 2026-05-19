package dev.mrraxyer.sportstournamentmanager.controllers;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.models.Partido;
import dev.mrraxyer.sportstournamentmanager.models.TablaPosiciones;
import dev.mrraxyer.sportstournamentmanager.services.impl.TorneoService;
import dev.mrraxyer.sportstournamentmanager.services.impl.PartidoService;
import dev.mrraxyer.sportstournamentmanager.services.impl.TablaPosicionesService;
import dev.mrraxyer.sportstournamentmanager.services.MatchSchedulerService;
import dev.mrraxyer.sportstournamentmanager.repositories.EquipoRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.PartidoRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.TablaPosicionesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TorneoController endpoints:
 * - GET /api/torneos/{id}/equipos
 * - PATCH /api/torneos/{id}/estado
 */
@ExtendWith(MockitoExtension.class)
class TorneoControllerTest {

    @Mock
    private TorneoService torneoService;

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private PartidoRepository partidoRepository;

    @Mock
    private TablaPosicionesRepository tablaPosicionesRepository;

    @Mock
    private MatchSchedulerService matchSchedulerService;

    @Mock
    private PartidoService partidoService;

    @Mock
    private TablaPosicionesService tablaPosicionesService;

    @InjectMocks
    private TorneoController torneoController;

    private Torneo torneo;
    private List<Equipo> equiposList;

    @BeforeEach
    void setUp() {
        // Initialize test data
        torneo = new Torneo();
        torneo.setTorneosId(1);
        torneo.setNombre("Copa Nacional 2024");
        torneo.setTipoFormato("grupos");
        torneo.setFechaInicio(LocalDate.of(2024, 6, 1));
        torneo.setEstado("BORRADOR");
        torneo.setNumGrupos(2);

        equiposList = new ArrayList<>();
        Equipo eq1 = new Equipo();
        eq1.setEquiposId(10);
        eq1.setNombre("Equipo A");
        eq1.setTorneo(torneo);

        Equipo eq2 = new Equipo();
        eq2.setEquiposId(11);
        eq2.setNombre("Equipo B");
        eq2.setTorneo(torneo);

        Equipo eq3 = new Equipo();
        eq3.setEquiposId(12);
        eq3.setNombre("Equipo C");
        eq3.setTorneo(torneo);

        equiposList.add(eq1);
        equiposList.add(eq2);
        equiposList.add(eq3);
        torneo.setEquipos(equiposList);
    }

    // ========== GET /api/torneos/{id}/equipos Tests ==========

    /**
     * Test 1: GET /torneos/{id}/equipos - Success with teams
     * Verify that when a tournament exists with teams, returns 200 OK
     * with a list of EquipoDTO objects containing equiposId and nombre
     */
    @Test
    void testGetEquiposByIdSuccess() {
        // Arrange
        when(torneoService.findById(1)).thenReturn(Optional.of(torneo));
        when(equipoRepository.findByTorneo(torneo)).thenReturn(equiposList);

        // Act
        ResponseEntity<ApiResponse<java.util.List<Equipo>>> response =
                torneoController.listarEquiposPorTorneo(1);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertNotNull(response.getBody().getData());
        assertEquals(3, response.getBody().getData().size());

        List<Equipo> returnedEquipos = response.getBody().getData();
        assertEquals("Equipo A", returnedEquipos.get(0).getNombre());
        assertEquals(10, returnedEquipos.get(0).getEquiposId());
        assertEquals("Equipo B", returnedEquipos.get(1).getNombre());
        assertEquals(11, returnedEquipos.get(1).getEquiposId());
        assertEquals("Equipo C", returnedEquipos.get(2).getNombre());
        assertEquals(12, returnedEquipos.get(2).getEquiposId());

        verify(torneoService, times(1)).findById(1);
        verify(equipoRepository, times(1)).findByTorneo(torneo);
    }

    /**
     * Test 2: GET /torneos/{id}/equipos - Tournament not found
     * Verify that when a tournament doesn't exist, returns 404 Not Found
     */
    @Test
    void testGetEquiposByIdNotFound() {
        // Arrange
        when(torneoService.findById(999)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<java.util.List<Equipo>>> response =
                torneoController.listarEquiposPorTorneo(999);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertNull(response.getBody().getData());
        assertTrue(response.getBody().getMessage().contains("Torneo no encontrado"));

        verify(torneoService, times(1)).findById(999);
        verify(equipoRepository, never()).findByTorneo(any());
    }

    /**
     * Test 3: GET /torneos/{id}/equipos - Empty team list
     * Verify that when a tournament exists but has no teams, returns 200 OK
     * with an empty list
     */
    @Test
    void testGetEquiposByIdEmptyList() {
        // Arrange
        Torneo torneoPrueba = new Torneo();
        torneoPrueba.setTorneosId(2);
        torneoPrueba.setNombre("Torneo Sin Equipos");
        torneoPrueba.setEquipos(new ArrayList<>());

        when(torneoService.findById(2)).thenReturn(Optional.of(torneoPrueba));
        when(equipoRepository.findByTorneo(torneoPrueba)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<ApiResponse<java.util.List<Equipo>>> response =
                torneoController.listarEquiposPorTorneo(2);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertNotNull(response.getBody().getData());
        assertEquals(0, response.getBody().getData().size());

        verify(torneoService, times(1)).findById(2);
        verify(equipoRepository, times(1)).findByTorneo(torneoPrueba);
    }

    // ========== PATCH /api/torneos/{id}/estado Tests ==========

    /**
     * Test 4: PATCH /torneos/{id}/estado - Success transition BORRADOR to ACTIVO
     * Verify that when changing state from BORRADOR to ACTIVO, returns 200 OK
     * with updated tournament
     */
    @Test
    void testCambiarEstadoSuccess() {
        // Arrange
        torneo.setEstado("BORRADOR");
        Torneo actualizado = new Torneo();
        actualizado.setTorneosId(1);
        actualizado.setNombre("Copa Nacional 2024");
        actualizado.setTipoFormato("grupos");
        actualizado.setFechaInicio(LocalDate.of(2024, 6, 1));
        actualizado.setEstado("ACTIVO");
        actualizado.setNumGrupos(2);

        when(torneoService.findById(1)).thenReturn(Optional.of(torneo));
        when(torneoService.save(any(Torneo.class))).thenReturn(actualizado);

        // Act
        ResponseEntity<ApiResponse<Torneo>> response =
                torneoController.cambiarEstado(1, "ACTIVO");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertNotNull(response.getBody().getData());
        assertEquals("ACTIVO", response.getBody().getData().getEstado());
        assertEquals(1, response.getBody().getData().getTorneosId());

        verify(torneoService, times(1)).findById(1);
        verify(torneoService, times(1)).save(any(Torneo.class));
    }

    /**
     * Test 5: PATCH /torneos/{id}/estado - Invalid estado parameter
     * Verify that when passing an invalid estado value, returns 400 Bad Request
     */
    @Test
    void testCambiarEstadoInvalidEstado() {
        // Arrange
        when(torneoService.findById(1)).thenReturn(Optional.of(torneo));

        // Act
        ResponseEntity<ApiResponse<Torneo>> response =
                torneoController.cambiarEstado(1, "INVALIDO");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertNull(response.getBody().getData());
        assertTrue(response.getBody().getMessage().toLowerCase().contains("estado inválido"));

        verify(torneoService, times(1)).findById(1);
        verify(torneoService, never()).save(any(Torneo.class));
    }

    /**
     * Test 6: PATCH /torneos/{id}/estado - Tournament not found
     * Verify that when tournament doesn't exist, returns 404 Not Found
     */
    @Test
    void testCambiarEstadoTorneoNotFound() {
        // Arrange
        when(torneoService.findById(999)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<Torneo>> response =
                torneoController.cambiarEstado(999, "ACTIVO");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertNull(response.getBody().getData());
        assertTrue(response.getBody().getMessage().contains("Torneo no encontrado"));

        verify(torneoService, times(1)).findById(999);
        verify(torneoService, never()).save(any(Torneo.class));
    }

    /**
     * Test 7: PATCH /torneos/{id}/estado - Invalid state transition
     * Verify that attempting invalid transitions returns 409 Conflict
     * Example: ACTIVO -> BORRADOR (only forward transitions allowed)
     */
    @Test
    void testCambiarEstadoInvalidTransition() {
        // Arrange
        torneo.setEstado("ACTIVO");
        when(torneoService.findById(1)).thenReturn(Optional.of(torneo));

        // Act - Try to go backwards from ACTIVO to BORRADOR
        ResponseEntity<ApiResponse<Torneo>> response =
                torneoController.cambiarEstado(1, "BORRADOR");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertNull(response.getBody().getData());
        assertTrue(response.getBody().getMessage().toLowerCase().contains("transición"));

        verify(torneoService, times(1)).findById(1);
        verify(torneoService, never()).save(any(Torneo.class));
    }

    /**
     * Test 8: PATCH /torneos/{id}/estado - Success transition ACTIVO to FINALIZADO
     * Verify that changing state from ACTIVO to FINALIZADO works correctly
     */
    @Test
    void testCambiarEstadoActivoToFinalizado() {
        // Arrange
        torneo.setEstado("ACTIVO");
        Torneo actualizado = new Torneo();
        actualizado.setTorneosId(1);
        actualizado.setNombre("Copa Nacional 2024");
        actualizado.setTipoFormato("grupos");
        actualizado.setFechaInicio(LocalDate.of(2024, 6, 1));
        actualizado.setEstado("FINALIZADO");
        actualizado.setNumGrupos(2);

        when(torneoService.findById(1)).thenReturn(Optional.of(torneo));
        when(torneoService.save(any(Torneo.class))).thenReturn(actualizado);

        // Act
        ResponseEntity<ApiResponse<Torneo>> response =
                torneoController.cambiarEstado(1, "FINALIZADO");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertNotNull(response.getBody().getData());
        assertEquals("FINALIZADO", response.getBody().getData().getEstado());

        verify(torneoService, times(1)).findById(1);
        verify(torneoService, times(1)).save(any(Torneo.class));
    }

    /**
     * Test 9: PATCH /torneos/{id}/estado - Case insensitive estado parameter
     * Verify that estado parameter is handled case-insensitively
     */
    @Test
    void testCambiarEstadoCaseInsensitive() {
        // Arrange
        torneo.setEstado("BORRADOR");
        Torneo actualizado = new Torneo();
        actualizado.setTorneosId(1);
        actualizado.setNombre("Copa Nacional 2024");
        actualizado.setTipoFormato("grupos");
        actualizado.setFechaInicio(LocalDate.of(2024, 6, 1));
        actualizado.setEstado("ACTIVO");
        actualizado.setNumGrupos(2);

        when(torneoService.findById(1)).thenReturn(Optional.of(torneo));
        when(torneoService.save(any(Torneo.class))).thenReturn(actualizado);

        // Act - Pass lowercase estado
        ResponseEntity<ApiResponse<Torneo>> response =
                torneoController.cambiarEstado(1, "activo");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("ACTIVO", response.getBody().getData().getEstado());

        verify(torneoService, times(1)).findById(1);
        verify(torneoService, times(1)).save(any(Torneo.class));
    }

    /**
     * Test 10: PATCH /torneos/{id}/estado - No-op transition (same estado)
     * Verify that setting the same estado as current is allowed (idempotent)
     */
    @Test
    void testCambiarEstadoNoOp() {
        // Arrange
        torneo.setEstado("BORRADOR");
        Torneo actualizado = new Torneo();
        actualizado.setTorneosId(1);
        actualizado.setNombre("Copa Nacional 2024");
        actualizado.setTipoFormato("grupos");
        actualizado.setFechaInicio(LocalDate.of(2024, 6, 1));
        actualizado.setEstado("BORRADOR");
        actualizado.setNumGrupos(2);

        when(torneoService.findById(1)).thenReturn(Optional.of(torneo));
        when(torneoService.save(any(Torneo.class))).thenReturn(actualizado);

        // Act - Set to same estado
        ResponseEntity<ApiResponse<Torneo>> response =
                torneoController.cambiarEstado(1, "BORRADOR");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatus());
        assertEquals("BORRADOR", response.getBody().getData().getEstado());

        verify(torneoService, times(1)).findById(1);
        verify(torneoService, times(1)).save(any(Torneo.class));
    }

    // ========== POST /api/torneos/{id}/avanzar-eliminatoria Tests ==========

    /**
     * Test 11: POST avanzar-eliminatoria success with 2 clasificados per group
     * Verify that advancing from group stage to elimination bracket works
     * Returns 201 Created with list of elimination matches
     */
    @Test
    void testAvanzarEliminatoriaSuccess() {
        // Arrange
        torneo.setEstado("ACTIVO");
        torneo.setTipoFormato("grupos");
        torneo.setNumGrupos(2);

        Equipo eq1 = new Equipo();
        eq1.setEquiposId(10);
        eq1.setNombre("Equipo A");

        Equipo eq2 = new Equipo();
        eq2.setEquiposId(11);
        eq2.setNombre("Equipo B");

        Equipo eq3 = new Equipo();
        eq3.setEquiposId(12);
        eq3.setNombre("Equipo C");

        Equipo eq4 = new Equipo();
        eq4.setEquiposId(13);
        eq4.setNombre("Equipo D");

        TablaPosiciones tp1 = new TablaPosiciones();
        tp1.setTablaPosicionesId(1);
        tp1.setEquipo(eq1);
        tp1.setPuntos(9);
        tp1.setGrupo("A");

        TablaPosiciones tp2 = new TablaPosiciones();
        tp2.setTablaPosicionesId(2);
        tp2.setEquipo(eq2);
        tp2.setPuntos(6);
        tp2.setGrupo("A");

        TablaPosiciones tp3 = new TablaPosiciones();
        tp3.setTablaPosicionesId(3);
        tp3.setEquipo(eq3);
        tp3.setPuntos(9);
        tp3.setGrupo("B");

        TablaPosiciones tp4 = new TablaPosiciones();
        tp4.setTablaPosicionesId(4);
        tp4.setEquipo(eq4);
        tp4.setPuntos(6);
        tp4.setGrupo("B");

        List<TablaPosiciones> standings = new ArrayList<>();
        standings.add(tp1);
        standings.add(tp2);
        standings.add(tp3);
        standings.add(tp4);

        Partido p1 = new Partido();
        p1.setPartidosId(101);
        p1.setEquipoLocal(eq1);
        p1.setEquipoVisitante(eq3);

        Partido p2 = new Partido();
        p2.setPartidosId(102);
        p2.setEquipoLocal(eq2);
        p2.setEquipoVisitante(eq4);

        List<Partido> eliminatoriaPartidos = new ArrayList<>();
        eliminatoriaPartidos.add(p1);
        eliminatoriaPartidos.add(p2);

        when(torneoService.findById(1)).thenReturn(Optional.of(torneo));

        // Mock repository method calls for groups A and B
        List<TablaPosiciones> grupoA = new ArrayList<>();
        grupoA.add(tp1);
        grupoA.add(tp2);

        List<TablaPosiciones> grupoB = new ArrayList<>();
        grupoB.add(tp3);
        grupoB.add(tp4);

        when(tablaPosicionesRepository.findByTorneoAndGrupo(torneo, "A")).thenReturn(grupoA);
        when(tablaPosicionesRepository.findByTorneoAndGrupo(torneo, "B")).thenReturn(grupoB);
        when(matchSchedulerService.scheduleMatches(eq(torneo), anyList(), any(), any()))
                .thenReturn(eliminatoriaPartidos);
        when(partidoService.saveAll(any())).thenReturn(eliminatoriaPartidos);

        // Act
        ResponseEntity<ApiResponse<List<Partido>>> response =
                torneoController.avanzarEliminatoria(1, 2);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(201, response.getBody().getStatus());
        assertNotNull(response.getBody().getData());
        assertEquals(2, response.getBody().getData().size());

        verify(torneoService, times(1)).findById(1);
        verify(tablaPosicionesRepository, times(1)).findByTorneoAndGrupo(any(), eq("A"));
        verify(tablaPosicionesRepository, times(1)).findByTorneoAndGrupo(any(), eq("B"));
        verify(matchSchedulerService, times(1)).scheduleMatches(any(), any(), any(), any());
        verify(partidoService, times(1)).saveAll(any());
    }

    /**
     * Test 12: POST avanzar-eliminatoria tournament not found
     * Verify that 404 is returned when tournament doesn't exist
     */
    @Test
    void testAvanzarEliminatoriaNotFound() {
        // Arrange
        when(torneoService.findById(999)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<List<Partido>>> response =
                torneoController.avanzarEliminatoria(999, 2);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertNull(response.getBody().getData());
        assertTrue(response.getBody().getMessage().contains("Torneo no encontrado"));

        verify(torneoService, times(1)).findById(999);
    }

    /**
     * Test 13: POST avanzar-eliminatoria invalid formato (not grupo/grupos)
     * Verify that 409 Conflict is returned when tournament is not group format
     */
    @Test
    void testAvanzarEliminatoriaInvalidFormato() {
        // Arrange
        torneo.setTipoFormato("single-elimination");
        when(torneoService.findById(1)).thenReturn(Optional.of(torneo));

        // Act
        ResponseEntity<ApiResponse<List<Partido>>> response =
                torneoController.avanzarEliminatoria(1, 2);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertNull(response.getBody().getData());
        assertTrue(response.getBody().getMessage().contains("GRUPOS"));

        verify(torneoService, times(1)).findById(1);
    }

    /**
     * Test 14: POST avanzar-eliminatoria with custom clasificados per group
     * Verify that endpoint respects the clasificadosPorGrupo parameter
     */
    @Test
    void testAvanzarEliminatoriaWithCustomClasificados() {
        // Arrange
        torneo.setEstado("ACTIVO");
        torneo.setTipoFormato("grupos");
        torneo.setNumGrupos(2);

        Equipo eq1 = new Equipo();
        eq1.setEquiposId(10);
        eq1.setNombre("Team 1");

        Equipo eq2 = new Equipo();
        eq2.setEquiposId(11);
        eq2.setNombre("Team 2");

        Equipo eq3 = new Equipo();
        eq3.setEquiposId(12);
        eq3.setNombre("Team 3");

        Equipo eq4 = new Equipo();
        eq4.setEquiposId(13);
        eq4.setNombre("Team 4");

        TablaPosiciones tp1 = new TablaPosiciones();
        tp1.setEquipo(eq1);
        tp1.setGrupo("A");
        tp1.setPuntos(12);

        TablaPosiciones tp2 = new TablaPosiciones();
        tp2.setEquipo(eq2);
        tp2.setGrupo("A");
        tp2.setPuntos(8);

        TablaPosiciones tp3 = new TablaPosiciones();
        tp3.setEquipo(eq3);
        tp3.setGrupo("B");
        tp3.setPuntos(10);

        TablaPosiciones tp4 = new TablaPosiciones();
        tp4.setEquipo(eq4);
        tp4.setGrupo("B");
        tp4.setPuntos(7);

        List<TablaPosiciones> standings = new ArrayList<>();
        standings.add(tp1);
        standings.add(tp2);
        standings.add(tp3);
        standings.add(tp4);

        Partido p1 = new Partido();
        p1.setPartidosId(201);

        List<Partido> eliminatoriaPartidos = new ArrayList<>();
        eliminatoriaPartidos.add(p1);

        when(torneoService.findById(1)).thenReturn(Optional.of(torneo));

        // Mock repository method calls for groups A and B
        List<TablaPosiciones> grupoA = new ArrayList<>();
        grupoA.add(tp1);
        grupoA.add(tp2);

        List<TablaPosiciones> grupoB = new ArrayList<>();
        grupoB.add(tp3);
        grupoB.add(tp4);

        when(tablaPosicionesRepository.findByTorneoAndGrupo(torneo, "A")).thenReturn(grupoA);
        when(tablaPosicionesRepository.findByTorneoAndGrupo(torneo, "B")).thenReturn(grupoB);
        when(matchSchedulerService.scheduleMatches(any(), any(), any(), any()))
                .thenReturn(eliminatoriaPartidos);
        when(partidoService.saveAll(any())).thenReturn(eliminatoriaPartidos);

        // Act
        ResponseEntity<ApiResponse<List<Partido>>> response =
                torneoController.avanzarEliminatoria(1, 1); // Only 1 per group

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(201, response.getBody().getStatus());

        verify(torneoService, times(1)).findById(1);
        verify(tablaPosicionesRepository, times(1)).findByTorneoAndGrupo(any(), eq("A"));
        verify(tablaPosicionesRepository, times(1)).findByTorneoAndGrupo(any(), eq("B"));
    }
}

package dev.mrraxyer.sportstournamentmanager.services;

import dev.mrraxyer.sportstournamentmanager.events.PartidoResultadoEvent;
import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.models.TablaPosiciones;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.repositories.EquipoRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.TablaPosicionesRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.TorneoRepository;
import dev.mrraxyer.sportstournamentmanager.services.impl.TablaPosicionesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TablaPosicionesService
 * Tests V/E/D tracking and grupo propagation
 */
@ExtendWith(MockitoExtension.class)
class TablaPosicionesServiceTest {

    @Mock
    private TablaPosicionesRepository tablaPosicionesRepository;

    @Mock
    private TorneoRepository torneoRepository;

    @Mock
    private EquipoRepository equipoRepository;

    @InjectMocks
    private TablaPosicionesService tablaPosicionesService;

    private Torneo torneo;
    private Equipo equipoLocal;
    private Equipo equipoVisitante;

    @BeforeEach
    void setUp() {
        torneo = new Torneo();
        torneo.setTorneosId(1);
        torneo.setNombre("Torneo Test");

        equipoLocal = new Equipo();
        equipoLocal.setEquiposId(1);
        equipoLocal.setNombre("Team Local");

        equipoVisitante = new Equipo();
        equipoVisitante.setEquiposId(2);
        equipoVisitante.setNombre("Team Visitante");
    }

    /**
     * Test 1: V/E/D incremented correctly for local team win
     * Local team scores more goals → victorias++, puntos+=3
     */
    @Test
    void localTeamWinIncrementsVictoriesAndPoints() {
        // Arrange
        PartidoResultadoEvent evento = new PartidoResultadoEvent(
                this, 1, 1, 1, 2, 3, 1, null);

        when(torneoRepository.findById(1)).thenReturn(Optional.of(torneo));
        when(equipoRepository.findById(1)).thenReturn(Optional.of(equipoLocal));
        when(equipoRepository.findById(2)).thenReturn(Optional.of(equipoVisitante));

        TablaPosiciones nuevaTablaPosiciones = new TablaPosiciones();
        nuevaTablaPosiciones.setTablaPosicionesId(1);
        nuevaTablaPosiciones.setTorneo(torneo);
        nuevaTablaPosiciones.setEquipo(equipoLocal);
        nuevaTablaPosiciones.setPartidosJugados(0);
        nuevaTablaPosiciones.setPuntos(0);
        nuevaTablaPosiciones.setGolesAFavor(0);
        nuevaTablaPosiciones.setGolesEnContra(0);
        nuevaTablaPosiciones.setVictorias(0);
        nuevaTablaPosiciones.setEmpates(0);
        nuevaTablaPosiciones.setDerrotas(0);

        when(tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipoLocal))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(nuevaTablaPosiciones));

        TablaPosiciones nuevaTablaPosicionesVisitante = new TablaPosiciones();
        nuevaTablaPosicionesVisitante.setTablaPosicionesId(2);
        nuevaTablaPosicionesVisitante.setTorneo(torneo);
        nuevaTablaPosicionesVisitante.setEquipo(equipoVisitante);
        nuevaTablaPosicionesVisitante.setPartidosJugados(0);
        nuevaTablaPosicionesVisitante.setPuntos(0);
        nuevaTablaPosicionesVisitante.setGolesAFavor(0);
        nuevaTablaPosicionesVisitante.setGolesEnContra(0);
        nuevaTablaPosicionesVisitante.setVictorias(0);
        nuevaTablaPosicionesVisitante.setEmpates(0);
        nuevaTablaPosicionesVisitante.setDerrotas(0);

        when(tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipoVisitante))
                .thenReturn(Optional.empty());

        when(tablaPosicionesRepository.save(any(TablaPosiciones.class)))
                .thenReturn(nuevaTablaPosiciones)
                .thenReturn(nuevaTablaPosicionesVisitante);

        // Act
        tablaPosicionesService.onPartidoResultado(evento);

        // Assert
        ArgumentCaptor<TablaPosiciones> captor = ArgumentCaptor.forClass(TablaPosiciones.class);
        verify(tablaPosicionesRepository, times(2)).save(captor.capture());

        TablaPosiciones savedLocal = captor.getAllValues().get(0);
        assertEquals(1, savedLocal.getVictorias());
        assertEquals(0, savedLocal.getEmpates());
        assertEquals(0, savedLocal.getDerrotas());
        assertEquals(3, savedLocal.getPuntos());
        assertEquals(1, savedLocal.getPartidosJugados());
        assertEquals(3, savedLocal.getGolesAFavor());
        assertEquals(1, savedLocal.getGolesEnContra());

        TablaPosiciones savedVisitante = captor.getAllValues().get(1);
        assertEquals(0, savedVisitante.getVictorias());
        assertEquals(0, savedVisitante.getEmpates());
        assertEquals(1, savedVisitante.getDerrotas());
        assertEquals(0, savedVisitante.getPuntos());
        assertEquals(1, savedVisitante.getPartidosJugados());
        assertEquals(1, savedVisitante.getGolesAFavor());
        assertEquals(3, savedVisitante.getGolesEnContra());
    }

    /**
     * Test 2: V/E/D incremented correctly for draw
     * Both teams score same goals → empates++, puntos+=1
     */
    @Test
    void drawIncrementsEmpatAndPoints() {
        // Arrange
        PartidoResultadoEvent evento = new PartidoResultadoEvent(
                this, 1, 1, 1, 2, 2, 2, null);

        when(torneoRepository.findById(1)).thenReturn(Optional.of(torneo));
        when(equipoRepository.findById(1)).thenReturn(Optional.of(equipoLocal));
        when(equipoRepository.findById(2)).thenReturn(Optional.of(equipoVisitante));

        TablaPosiciones nuevaTablaPosiciones = new TablaPosiciones();
        nuevaTablaPosiciones.setTablaPosicionesId(1);
        nuevaTablaPosiciones.setTorneo(torneo);
        nuevaTablaPosiciones.setEquipo(equipoLocal);
        nuevaTablaPosiciones.setPartidosJugados(0);
        nuevaTablaPosiciones.setPuntos(0);
        nuevaTablaPosiciones.setGolesAFavor(0);
        nuevaTablaPosiciones.setGolesEnContra(0);
        nuevaTablaPosiciones.setVictorias(0);
        nuevaTablaPosiciones.setEmpates(0);
        nuevaTablaPosiciones.setDerrotas(0);

        when(tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipoLocal))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(nuevaTablaPosiciones));

        TablaPosiciones nuevaTablaPosicionesVisitante = new TablaPosiciones();
        nuevaTablaPosicionesVisitante.setTablaPosicionesId(2);
        nuevaTablaPosicionesVisitante.setTorneo(torneo);
        nuevaTablaPosicionesVisitante.setEquipo(equipoVisitante);
        nuevaTablaPosicionesVisitante.setPartidosJugados(0);
        nuevaTablaPosicionesVisitante.setPuntos(0);
        nuevaTablaPosicionesVisitante.setGolesAFavor(0);
        nuevaTablaPosicionesVisitante.setGolesEnContra(0);
        nuevaTablaPosicionesVisitante.setVictorias(0);
        nuevaTablaPosicionesVisitante.setEmpates(0);
        nuevaTablaPosicionesVisitante.setDerrotas(0);

        when(tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipoVisitante))
                .thenReturn(Optional.empty());

        when(tablaPosicionesRepository.save(any(TablaPosiciones.class)))
                .thenReturn(nuevaTablaPosiciones)
                .thenReturn(nuevaTablaPosicionesVisitante);

        // Act
        tablaPosicionesService.onPartidoResultado(evento);

        // Assert
        ArgumentCaptor<TablaPosiciones> captor = ArgumentCaptor.forClass(TablaPosiciones.class);
        verify(tablaPosicionesRepository, times(2)).save(captor.capture());

        TablaPosiciones saved = captor.getAllValues().get(0);
        assertEquals(0, saved.getVictorias());
        assertEquals(1, saved.getEmpates());
        assertEquals(0, saved.getDerrotas());
        assertEquals(1, saved.getPuntos());
        assertEquals(1, saved.getPartidosJugados());
        assertEquals(2, saved.getGolesAFavor());
        assertEquals(2, saved.getGolesEnContra());
    }

    /**
     * Test 3: V/E/D incremented correctly for local team loss
     * Local team scores fewer goals → derrotas++, puntos+=0
     */
    @Test
    void localTeamLossIncrementsDerrrotasAndNoPoints() {
        // Arrange
        PartidoResultadoEvent evento = new PartidoResultadoEvent(
                this, 1, 1, 1, 2, 1, 2, null);

        when(torneoRepository.findById(1)).thenReturn(Optional.of(torneo));
        when(equipoRepository.findById(1)).thenReturn(Optional.of(equipoLocal));
        when(equipoRepository.findById(2)).thenReturn(Optional.of(equipoVisitante));

        TablaPosiciones nuevaTablaPosiciones = new TablaPosiciones();
        nuevaTablaPosiciones.setTablaPosicionesId(1);
        nuevaTablaPosiciones.setTorneo(torneo);
        nuevaTablaPosiciones.setEquipo(equipoLocal);
        nuevaTablaPosiciones.setPartidosJugados(0);
        nuevaTablaPosiciones.setPuntos(0);
        nuevaTablaPosiciones.setGolesAFavor(0);
        nuevaTablaPosiciones.setGolesEnContra(0);
        nuevaTablaPosiciones.setVictorias(0);
        nuevaTablaPosiciones.setEmpates(0);
        nuevaTablaPosiciones.setDerrotas(0);

        when(tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipoLocal))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(nuevaTablaPosiciones));

        TablaPosiciones nuevaTablaPosicionesVisitante = new TablaPosiciones();
        nuevaTablaPosicionesVisitante.setTablaPosicionesId(2);
        nuevaTablaPosicionesVisitante.setTorneo(torneo);
        nuevaTablaPosicionesVisitante.setEquipo(equipoVisitante);
        nuevaTablaPosicionesVisitante.setPartidosJugados(0);
        nuevaTablaPosicionesVisitante.setPuntos(0);
        nuevaTablaPosicionesVisitante.setGolesAFavor(0);
        nuevaTablaPosicionesVisitante.setGolesEnContra(0);
        nuevaTablaPosicionesVisitante.setVictorias(0);
        nuevaTablaPosicionesVisitante.setEmpates(0);
        nuevaTablaPosicionesVisitante.setDerrotas(0);

        when(tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipoVisitante))
                .thenReturn(Optional.empty());

        when(tablaPosicionesRepository.save(any(TablaPosiciones.class)))
                .thenReturn(nuevaTablaPosiciones)
                .thenReturn(nuevaTablaPosicionesVisitante);

        // Act
        tablaPosicionesService.onPartidoResultado(evento);

        // Assert
        ArgumentCaptor<TablaPosiciones> captor = ArgumentCaptor.forClass(TablaPosiciones.class);
        verify(tablaPosicionesRepository, times(2)).save(captor.capture());

        TablaPosiciones savedLocal = captor.getAllValues().get(0);
        assertEquals(0, savedLocal.getVictorias());
        assertEquals(0, savedLocal.getEmpates());
        assertEquals(1, savedLocal.getDerrotas());
        assertEquals(0, savedLocal.getPuntos());
        assertEquals(1, savedLocal.getPartidosJugados());
        assertEquals(1, savedLocal.getGolesAFavor());
        assertEquals(2, savedLocal.getGolesEnContra());

        TablaPosiciones savedVisitante = captor.getAllValues().get(1);
        assertEquals(1, savedVisitante.getVictorias());
        assertEquals(0, savedVisitante.getEmpates());
        assertEquals(0, savedVisitante.getDerrotas());
        assertEquals(3, savedVisitante.getPuntos());
        assertEquals(1, savedVisitante.getPartidosJugados());
        assertEquals(2, savedVisitante.getGolesAFavor());
        assertEquals(1, savedVisitante.getGolesEnContra());
    }

    /**
     * Test 4: grupo field set correctly on TablaPosiciones when passed from event
     * Verifies that grupo is propagated from event to TablaPosiciones
     */
    @Test
    void grupoFieldSetFromEvent() {
        // Arrange
        PartidoResultadoEvent evento = new PartidoResultadoEvent(
                this, 1, 1, 1, 2, 2, 1, "Grupo A");

        when(torneoRepository.findById(1)).thenReturn(Optional.of(torneo));
        when(equipoRepository.findById(1)).thenReturn(Optional.of(equipoLocal));
        when(equipoRepository.findById(2)).thenReturn(Optional.of(equipoVisitante));

        TablaPosiciones nuevaTablaPosiciones = new TablaPosiciones();
        nuevaTablaPosiciones.setTablaPosicionesId(1);
        nuevaTablaPosiciones.setTorneo(torneo);
        nuevaTablaPosiciones.setEquipo(equipoLocal);
        nuevaTablaPosiciones.setPartidosJugados(0);
        nuevaTablaPosiciones.setPuntos(0);
        nuevaTablaPosiciones.setGolesAFavor(0);
        nuevaTablaPosiciones.setGolesEnContra(0);
        nuevaTablaPosiciones.setGrupo("Grupo A");

        when(tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipoLocal))
                .thenReturn(Optional.empty());

        TablaPosiciones nuevaTablaPosicionesVisitante = new TablaPosiciones();
        nuevaTablaPosicionesVisitante.setTablaPosicionesId(2);
        nuevaTablaPosicionesVisitante.setTorneo(torneo);
        nuevaTablaPosicionesVisitante.setEquipo(equipoVisitante);
        nuevaTablaPosicionesVisitante.setPartidosJugados(0);
        nuevaTablaPosicionesVisitante.setPuntos(0);
        nuevaTablaPosicionesVisitante.setGolesAFavor(0);
        nuevaTablaPosicionesVisitante.setGolesEnContra(0);
        nuevaTablaPosicionesVisitante.setGrupo("Grupo A");

        when(tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipoVisitante))
                .thenReturn(Optional.empty());

        when(tablaPosicionesRepository.save(any(TablaPosiciones.class)))
                .thenReturn(nuevaTablaPosiciones)
                .thenReturn(nuevaTablaPosicionesVisitante);

        // Act
        tablaPosicionesService.onPartidoResultado(evento);

        // Assert
        ArgumentCaptor<TablaPosiciones> captor = ArgumentCaptor.forClass(TablaPosiciones.class);
        verify(tablaPosicionesRepository, times(2)).save(captor.capture());

        TablaPosiciones savedLocal = captor.getAllValues().get(0);
        assertEquals("Grupo A", savedLocal.getGrupo());

        TablaPosiciones savedVisitante = captor.getAllValues().get(1);
        assertEquals("Grupo A", savedVisitante.getGrupo());
    }

    /**
     * Test 5: existing TablaPosiciones updated (V/E/D incremented) when team plays again
     * Verifies that counters accumulate across multiple matches
     */
    @Test
    void existingTablaPosicionesUpdatedOnSecondMatch() {
        // Arrange - First match: Local team won
        PartidoResultadoEvent firstEvent = new PartidoResultadoEvent(
                this, 1, 1, 1, 2, 3, 1, null);

        when(torneoRepository.findById(1)).thenReturn(Optional.of(torneo));
        when(equipoRepository.findById(1)).thenReturn(Optional.of(equipoLocal));
        when(equipoRepository.findById(2)).thenReturn(Optional.of(equipoVisitante));

        // Existing tabla for local team from first match
        TablaPosiciones existingLocal = new TablaPosiciones();
        existingLocal.setTablaPosicionesId(1);
        existingLocal.setTorneo(torneo);
        existingLocal.setEquipo(equipoLocal);
        existingLocal.setPartidosJugados(1);
        existingLocal.setPuntos(3);
        existingLocal.setGolesAFavor(3);
        existingLocal.setGolesEnContra(1);
        existingLocal.setVictorias(1);
        existingLocal.setEmpates(0);
        existingLocal.setDerrotas(0);

        TablaPosiciones existingVisitante = new TablaPosiciones();
        existingVisitante.setTablaPosicionesId(2);
        existingVisitante.setTorneo(torneo);
        existingVisitante.setEquipo(equipoVisitante);
        existingVisitante.setPartidosJugados(1);
        existingVisitante.setPuntos(0);
        existingVisitante.setGolesAFavor(1);
        existingVisitante.setGolesEnContra(3);
        existingVisitante.setVictorias(0);
        existingVisitante.setEmpates(0);
        existingVisitante.setDerrotas(1);

        // Second match: Draw
        PartidoResultadoEvent secondEvent = new PartidoResultadoEvent(
                this, 2, 1, 1, 2, 2, 2, null);

        when(tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipoLocal))
                .thenReturn(Optional.of(existingLocal));
        when(tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipoVisitante))
                .thenReturn(Optional.of(existingVisitante));

        when(tablaPosicionesRepository.save(any(TablaPosiciones.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act - Process second event
        tablaPosicionesService.onPartidoResultado(secondEvent);

        // Assert
        ArgumentCaptor<TablaPosiciones> captor = ArgumentCaptor.forClass(TablaPosiciones.class);
        verify(tablaPosicionesRepository, times(2)).save(captor.capture());

        TablaPosiciones savedLocal = captor.getAllValues().get(0);
        assertEquals(2, savedLocal.getPartidosJugados()); // 1 + 1
        assertEquals(4, savedLocal.getPuntos()); // 3 + 1 (draw)
        assertEquals(1, savedLocal.getVictorias()); // unchanged
        assertEquals(1, savedLocal.getEmpates()); // 0 + 1
        assertEquals(0, savedLocal.getDerrotas()); // unchanged
        assertEquals(5, savedLocal.getGolesAFavor()); // 3 + 2
        assertEquals(3, savedLocal.getGolesEnContra()); // 1 + 2

        TablaPosiciones savedVisitante = captor.getAllValues().get(1);
        assertEquals(2, savedVisitante.getPartidosJugados()); // 1 + 1
        assertEquals(1, savedVisitante.getPuntos()); // 0 + 1 (draw)
        assertEquals(0, savedVisitante.getVictorias()); // unchanged
        assertEquals(1, savedVisitante.getEmpates()); // 0 + 1
        assertEquals(1, savedVisitante.getDerrotas()); // unchanged
        assertEquals(3, savedVisitante.getGolesAFavor()); // 1 + 2
        assertEquals(5, savedVisitante.getGolesEnContra()); // 3 + 2
    }

    /**
     * Test 6: evento with null grupo (non-group tournament)
     * Verifies graceful handling of null grupo
     */
    @Test
    void handlesNullGrupoGracefully() {
        // Arrange
        PartidoResultadoEvent evento = new PartidoResultadoEvent(
                this, 1, 1, 1, 2, 2, 1, null);

        when(torneoRepository.findById(1)).thenReturn(Optional.of(torneo));
        when(equipoRepository.findById(1)).thenReturn(Optional.of(equipoLocal));
        when(equipoRepository.findById(2)).thenReturn(Optional.of(equipoVisitante));

        TablaPosiciones nuevaTablaPosiciones = new TablaPosiciones();
        nuevaTablaPosiciones.setTablaPosicionesId(1);
        nuevaTablaPosiciones.setTorneo(torneo);
        nuevaTablaPosiciones.setEquipo(equipoLocal);
        nuevaTablaPosiciones.setPartidosJugados(0);
        nuevaTablaPosiciones.setPuntos(0);
        nuevaTablaPosiciones.setGolesAFavor(0);
        nuevaTablaPosiciones.setGolesEnContra(0);
        nuevaTablaPosiciones.setGrupo(null);

        when(tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipoLocal))
                .thenReturn(Optional.empty());

        TablaPosiciones nuevaTablaPosicionesVisitante = new TablaPosiciones();
        nuevaTablaPosicionesVisitante.setTablaPosicionesId(2);
        nuevaTablaPosicionesVisitante.setTorneo(torneo);
        nuevaTablaPosicionesVisitante.setEquipo(equipoVisitante);
        nuevaTablaPosicionesVisitante.setPartidosJugados(0);
        nuevaTablaPosicionesVisitante.setPuntos(0);
        nuevaTablaPosicionesVisitante.setGolesAFavor(0);
        nuevaTablaPosicionesVisitante.setGolesEnContra(0);
        nuevaTablaPosicionesVisitante.setGrupo(null);

        when(tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipoVisitante))
                .thenReturn(Optional.empty());

        when(tablaPosicionesRepository.save(any(TablaPosiciones.class)))
                .thenReturn(nuevaTablaPosiciones)
                .thenReturn(nuevaTablaPosicionesVisitante);

        // Act
        tablaPosicionesService.onPartidoResultado(evento);

        // Assert - Should not throw exception
        ArgumentCaptor<TablaPosiciones> captor = ArgumentCaptor.forClass(TablaPosiciones.class);
        verify(tablaPosicionesRepository, times(2)).save(captor.capture());

        TablaPosiciones saved = captor.getAllValues().get(0);
        assertNull(saved.getGrupo());
        assertEquals(3, saved.getPuntos()); // Still calculates points correctly
    }

    /**
     * Test 7: V/E/D math consistency
     * Verifies that total matches = victorias + empates + derrotas
     * and that puntos = (victorias * 3) + empates
     */
    @Test
    void ved_MathIsConsistent() {
        // Arrange
        PartidoResultadoEvent evento = new PartidoResultadoEvent(
                this, 1, 1, 1, 2, 2, 0, null);

        when(torneoRepository.findById(1)).thenReturn(Optional.of(torneo));
        when(equipoRepository.findById(1)).thenReturn(Optional.of(equipoLocal));
        when(equipoRepository.findById(2)).thenReturn(Optional.of(equipoVisitante));

        TablaPosiciones nuevaTablaPosiciones = new TablaPosiciones();
        nuevaTablaPosiciones.setTablaPosicionesId(1);
        nuevaTablaPosiciones.setTorneo(torneo);
        nuevaTablaPosiciones.setEquipo(equipoLocal);
        nuevaTablaPosiciones.setPartidosJugados(0);
        nuevaTablaPosiciones.setPuntos(0);
        nuevaTablaPosiciones.setGolesAFavor(0);
        nuevaTablaPosiciones.setGolesEnContra(0);

        when(tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipoLocal))
                .thenReturn(Optional.empty());

        TablaPosiciones nuevaTablaPosicionesVisitante = new TablaPosiciones();
        nuevaTablaPosicionesVisitante.setTablaPosicionesId(2);
        nuevaTablaPosicionesVisitante.setTorneo(torneo);
        nuevaTablaPosicionesVisitante.setEquipo(equipoVisitante);
        nuevaTablaPosicionesVisitante.setPartidosJugados(0);
        nuevaTablaPosicionesVisitante.setPuntos(0);
        nuevaTablaPosicionesVisitante.setGolesAFavor(0);
        nuevaTablaPosicionesVisitante.setGolesEnContra(0);

        when(tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipoVisitante))
                .thenReturn(Optional.empty());

        when(tablaPosicionesRepository.save(any(TablaPosiciones.class)))
                .thenReturn(nuevaTablaPosiciones)
                .thenReturn(nuevaTablaPosicionesVisitante);

        // Act
        tablaPosicionesService.onPartidoResultado(evento);

        // Assert
        ArgumentCaptor<TablaPosiciones> captor = ArgumentCaptor.forClass(TablaPosiciones.class);
        verify(tablaPosicionesRepository, times(2)).save(captor.capture());

        TablaPosiciones saved = captor.getAllValues().get(0);
        // Math check: partidos = V + E + D
        assertEquals(
                saved.getPartidosJugados(),
                saved.getVictorias() + saved.getEmpates() + saved.getDerrotas());

        // Math check: puntos = V*3 + E*1
        int expectedPuntos = (saved.getVictorias() * 3) + (saved.getEmpates() * 1);
        assertEquals(expectedPuntos, saved.getPuntos());
    }

    /**
     * Test 8: findByTorneo returns all standings for tournament
     * Verifies that the method queries repository by torneo ID and returns full objects
     */
    @Test
    void findByTorneoReturnsAllStandings() {
        // Arrange
        TablaPosiciones tp1 = new TablaPosiciones();
        tp1.setTablaPosicionesId(1);
        tp1.setTorneo(torneo);
        tp1.setEquipo(equipoLocal);
        tp1.setPuntos(6);
        tp1.setGrupo("A");

        TablaPosiciones tp2 = new TablaPosiciones();
        tp2.setTablaPosicionesId(2);
        tp2.setTorneo(torneo);
        tp2.setEquipo(equipoVisitante);
        tp2.setPuntos(3);
        tp2.setGrupo("A");

        java.util.List<TablaPosiciones> standings = java.util.Arrays.asList(tp1, tp2);

        when(torneoRepository.findById(1)).thenReturn(Optional.of(torneo));
        when(tablaPosicionesRepository.findByTorneo(torneo)).thenReturn(standings);

        // Act
        java.util.List<TablaPosiciones> result = tablaPosicionesService.findByTorneo(1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(6, result.get(0).getPuntos());
        assertEquals("A", result.get(0).getGrupo());
        assertEquals(3, result.get(1).getPuntos());
        assertEquals("A", result.get(1).getGrupo());

        verify(torneoRepository, times(1)).findById(1);
        verify(tablaPosicionesRepository, times(1)).findByTorneo(torneo);
    }

    /**
     * Test 9: findByTorneo returns empty list if tournament has no standings
     * Verifies that the method returns empty list when no standings exist
     */
    @Test
    void findByTorneoReturnsEmptyListWhenNoStandings() {
        // Arrange
        when(torneoRepository.findById(1)).thenReturn(Optional.of(torneo));
        when(tablaPosicionesRepository.findByTorneo(torneo)).thenReturn(new java.util.ArrayList<>());

        // Act
        java.util.List<TablaPosiciones> result = tablaPosicionesService.findByTorneo(1);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());

        verify(torneoRepository, times(1)).findById(1);
        verify(tablaPosicionesRepository, times(1)).findByTorneo(torneo);
    }

    /**
     * Test 10: findByTorneo returns empty list if tournament not found
     * Verifies that the method returns empty list for non-existent tournament
     */
    @Test
    void findByTorneoReturnsEmptyListIfTorneoNotFound() {
        // Arrange
        when(torneoRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        java.util.List<TablaPosiciones> result = tablaPosicionesService.findByTorneo(999);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());

        verify(torneoRepository, times(1)).findById(999);
        verify(tablaPosicionesRepository, never()).findByTorneo(any());
    }
}

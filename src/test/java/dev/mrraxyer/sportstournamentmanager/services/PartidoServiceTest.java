package dev.mrraxyer.sportstournamentmanager.services;

import dev.mrraxyer.sportstournamentmanager.events.PartidoResultadoEvent;
import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.models.Partido;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.repositories.PartidoRepository;
import dev.mrraxyer.sportstournamentmanager.services.impl.PartidoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PartidoService event publishing logic
 */
@ExtendWith(MockitoExtension.class)
class PartidoServiceTest {

    @Mock
    private PartidoRepository partidoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PartidoService partidoService;

    /**
     * Test 1: Verifica que NO se publique evento cuando jugado==false
     * Simula la creación de un partido durante la generación del calendario
     */
    @Test
    void noPublicaEventoCuandoJugadoEsFalse() {
        // Arrange
        Partido partido = createPartidoNotPlayed();
        when(partidoRepository.save(partido)).thenReturn(partido);

        // Act
        Partido resultado = partidoService.save(partido);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.getJugado());
        verify(eventPublisher, never()).publishEvent(any(PartidoResultadoEvent.class));
    }

    /**
     * Test 2: Verifica que se publique evento cuando jugado==true
     * Simula el registro de un resultado de partido
     */
    @Test
    void publicaEventoCuandoJugadoEsTrue() {
        // Arrange
        Partido partido = createPartidoPlayed();
        when(partidoRepository.save(partido)).thenReturn(partido);

        // Act
        Partido resultado = partidoService.save(partido);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.getJugado());
        verify(eventPublisher, times(1)).publishEvent(any(PartidoResultadoEvent.class));
    }

    /**
     * Test 3: Verifica que el evento incluya el campo grupo
     * Valida que la información de grupo se transmita correctamente en el evento
     */
    @Test
    void eventoContieneGrupo() {
        // Arrange
        Partido partido = createPartidoPlayedWithGrupo("Grupo A");
        when(partidoRepository.save(partido)).thenReturn(partido);

        ArgumentCaptor<PartidoResultadoEvent> eventCaptor = ArgumentCaptor.forClass(PartidoResultadoEvent.class);

        // Act
        Partido resultado = partidoService.save(partido);

        // Assert
        assertNotNull(resultado);
        assertEquals("Grupo A", resultado.getGrupo());
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        PartidoResultadoEvent evento = eventCaptor.getValue();
        assertEquals("Grupo A", evento.getGrupo());
    }

    /**
     * Helper: Crea un partido no jugado (jugado=false)
     * Simula un partido del calendario sin resultado
     */
    private Partido createPartidoNotPlayed() {
        Partido partido = new Partido();
        partido.setPartidosId(1);
        partido.setJugado(false);
        partido.setGolesLocal(0);
        partido.setGolesVisitante(0);
        partido.setFechaPartido(LocalDateTime.now());
        partido.setGrupo(null);

        Torneo torneo = new Torneo();
        torneo.setTorneosId(1);
        partido.setTorneo(torneo);

        Equipo equipoLocal = new Equipo();
        equipoLocal.setEquiposId(1);
        partido.setEquipoLocal(equipoLocal);

        Equipo equipoVisitante = new Equipo();
        equipoVisitante.setEquiposId(2);
        partido.setEquipoVisitante(equipoVisitante);

        return partido;
    }

    /**
     * Helper: Crea un partido jugado (jugado=true) sin grupo específico
     * Simula un partido con resultado registrado
     */
    private Partido createPartidoPlayed() {
        Partido partido = new Partido();
        partido.setPartidosId(2);
        partido.setJugado(true);
        partido.setGolesLocal(2);
        partido.setGolesVisitante(1);
        partido.setFechaPartido(LocalDateTime.now());
        partido.setGrupo(null);

        Torneo torneo = new Torneo();
        torneo.setTorneosId(1);
        partido.setTorneo(torneo);

        Equipo equipoLocal = new Equipo();
        equipoLocal.setEquiposId(1);
        partido.setEquipoLocal(equipoLocal);

        Equipo equipoVisitante = new Equipo();
        equipoVisitante.setEquiposId(2);
        partido.setEquipoVisitante(equipoVisitante);

        return partido;
    }

    /**
     * Helper: Crea un partido jugado con grupo específico
     * Simula un partido con resultado registrado en un grupo determinado
     */
    private Partido createPartidoPlayedWithGrupo(String grupo) {
        Partido partido = new Partido();
        partido.setPartidosId(3);
        partido.setJugado(true);
        partido.setGolesLocal(3);
        partido.setGolesVisitante(0);
        partido.setFechaPartido(LocalDateTime.now());
        partido.setGrupo(grupo);

        Torneo torneo = new Torneo();
        torneo.setTorneosId(1);
        partido.setTorneo(torneo);

        Equipo equipoLocal = new Equipo();
        equipoLocal.setEquiposId(1);
        partido.setEquipoLocal(equipoLocal);

        Equipo equipoVisitante = new Equipo();
        equipoVisitante.setEquiposId(2);
        partido.setEquipoVisitante(equipoVisitante);

        return partido;
    }
}

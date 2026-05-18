package dev.mrraxyer.sportstournamentmanager.services;

import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.models.Partido;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.repositories.EquipoRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.PartidoRepository;
import dev.mrraxyer.sportstournamentmanager.strategies.MatchScheduleStrategy;
import dev.mrraxyer.sportstournamentmanager.strategies.RoundRobinScheduleStrategy;
import dev.mrraxyer.sportstournamentmanager.strategies.SingleEliminationScheduleStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio orquestador para la generación de calendarios (con Strategy).
 */
@Service
public class MatchSchedulerService {

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    /**
     * Genera y persiste partidos usando la estrategia proporcionada.
     */
    @Transactional
    public List<Partido> scheduleMatches(Torneo torneo, List<Equipo> equipos, MatchScheduleStrategy strategy, LocalDate startDate) {
        List<Partido> partidos = strategy.generateSchedule(torneo, equipos, startDate);
        if (partidos.isEmpty()) {
            return partidos;
        }
        return partidoRepository.saveAll(partidos);
    }

    /**
     * Genera el calendario agrupado por jornadas/rondas sin persistirlo.
     */
    public List<List<Partido>> buildGroupedSchedule(Torneo torneo, List<Equipo> equipos, MatchScheduleStrategy strategy, LocalDate startDate) {
        return strategy.generateGroupedSchedule(torneo, equipos, startDate);
    }

    /**
     * Determina una estrategia por defecto según el tipo de formato del torneo
     * y genera/persiste el calendario usando los equipos del torneo.
     */
    @Transactional
    public List<Partido> scheduleMatchesUsingTorneoFormat(Torneo torneo, LocalDate startDate) {
        List<Equipo> equipos = equipoRepository.findByTorneo(torneo);
        MatchScheduleStrategy strategy;
        String tipo = torneo.getTipoFormato() == null ? "" : torneo.getTipoFormato().toLowerCase();
        if (tipo.contains("liga") || tipo.contains("round") || tipo.contains("round-robin")) {
            strategy = new RoundRobinScheduleStrategy();
        } else {
            // por defecto asumimos eliminación directa
            strategy = new SingleEliminationScheduleStrategy();
        }
        return scheduleMatches(torneo, equipos, strategy, startDate);
    }

    /**
     * Devuelve el bracket/calendario agrupado según el formato del torneo.
     */
    public List<List<Partido>> buildGroupedScheduleUsingTorneoFormat(Torneo torneo, LocalDate startDate) {
        List<Equipo> equipos = equipoRepository.findByTorneo(torneo);
        MatchScheduleStrategy strategy;
        String tipo = torneo.getTipoFormato() == null ? "" : torneo.getTipoFormato().toLowerCase();
        if (tipo.contains("liga") || tipo.contains("round") || tipo.contains("round-robin")) {
            strategy = new RoundRobinScheduleStrategy();
        } else {
            strategy = new SingleEliminationScheduleStrategy();
        }
        return new ArrayList<>(buildGroupedSchedule(torneo, equipos, strategy, startDate));
    }
}


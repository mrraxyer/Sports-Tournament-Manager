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

    @Autowired
    private dev.mrraxyer.sportstournamentmanager.repositories.TablaPosicionesRepository tablaPosicionesRepository;

    /**
     * Genera y persiste partidos usando la estrategia proporcionada.
     */
    @Transactional
    public List<Partido> scheduleMatches(Torneo torneo, List<Equipo> equipos, MatchScheduleStrategy strategy,
            LocalDate startDate) {
        List<Partido> partidos = strategy.generateSchedule(torneo, equipos, startDate);
        if (partidos.isEmpty()) {
            return partidos;
        }
        return partidoRepository.saveAll(partidos);
    }

    /**
     * Genera el calendario agrupado por jornadas/rondas sin persistirlo.
     */
    public List<List<Partido>> buildGroupedSchedule(Torneo torneo, List<Equipo> equipos, MatchScheduleStrategy strategy,
            LocalDate startDate) {
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
        } else if (tipo.contains("grupos") || tipo.contains("grupo")) {
            strategy = new dev.mrraxyer.sportstournamentmanager.strategies.GroupStageScheduleStrategy();
        } else {
            // por defecto asumimos eliminación directa
            strategy = new SingleEliminationScheduleStrategy();
        }
        List<Partido> partidos = scheduleMatches(torneo, equipos, strategy, startDate);

        // Si es formato de grupos, crear las entradas iniciales en tabla_posiciones con
        // el grupo asignado
        if (tipo.contains("grupos") || tipo.contains("grupo")) {
            Integer numGrupos = torneo.getNumGrupos() == null ? 2 : torneo.getNumGrupos();
            List<List<Equipo>> grupos = new java.util.ArrayList<>();
            for (int i = 0; i < numGrupos; i++)
                grupos.add(new java.util.ArrayList<>());
            for (int i = 0; i < equipos.size(); i++) {
                grupos.get(i % numGrupos).add(equipos.get(i));
            }
            char letra = 'A';
            for (List<Equipo> grupo : grupos) {
                String codigo = String.valueOf(letra);
                for (Equipo e : grupo) {
                    // crear entrada en tabla de posiciones si no existe
                    java.util.Optional<dev.mrraxyer.sportstournamentmanager.models.TablaPosiciones> tpOpt = tablaPosicionesRepository
                            .findByTorneoAndEquipo(torneo, e);
                    if (!tpOpt.isPresent()) {
                        dev.mrraxyer.sportstournamentmanager.models.TablaPosiciones tp = new dev.mrraxyer.sportstournamentmanager.models.TablaPosiciones();
                        tp.setTorneo(torneo);
                        tp.setEquipo(e);
                        tp.setPartidosJugados(0);
                        tp.setPuntos(0);
                        tp.setGolesAFavor(0);
                        tp.setGolesEnContra(0);
                        tp.setGrupo(codigo);
                        tp.setVictorias(0);
                        tp.setEmpates(0);
                        tp.setDerrotas(0);
                        tablaPosicionesRepository.save(tp);
                    } else {
                        // actualizar grupo en caso de estar vacío
                        dev.mrraxyer.sportstournamentmanager.models.TablaPosiciones tp = tpOpt.get();
                        if (tp.getGrupo() == null) {
                            tp.setGrupo(codigo);
                            tablaPosicionesRepository.save(tp);
                        }
                    }
                }
                letra++;
            }
        }

        return partidos;
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

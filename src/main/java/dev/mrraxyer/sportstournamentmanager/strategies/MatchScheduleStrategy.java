package dev.mrraxyer.sportstournamentmanager.strategies;

import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.models.Partido;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;

import java.time.LocalDate;
import java.util.List;

/** Interfaz Strategy para generar calendarios de partidos (sin persistir). */
public interface MatchScheduleStrategy {

    /** Genera la lista de partidos para el torneo dado. */
    List<Partido> generateSchedule(Torneo torneo, List<Equipo> equipos, LocalDate startDate);

    /** Genera el calendario agrupado por jornadas/rondas. */
    default List<List<Partido>> generateGroupedSchedule(Torneo torneo, List<Equipo> equipos, LocalDate startDate) {
        return java.util.Collections.singletonList(generateSchedule(torneo, equipos, startDate));
    }
}


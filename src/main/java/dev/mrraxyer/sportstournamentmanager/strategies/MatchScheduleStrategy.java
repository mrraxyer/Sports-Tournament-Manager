package dev.mrraxyer.sportstournamentmanager.strategies;

import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.models.Partido;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz Strategy para generación de calendarios de partidos.
 * Implementaciones deben generar una lista de objetos Partido (sin persistir).
 */
public interface MatchScheduleStrategy {

    /**
     * Genera el calendario de partidos para el torneo y la lista de equipos indicada.
     *
     * @param torneo    torneo para el que se generan los partidos
     * @param equipos   lista de equipos participantes
     * @param startDate fecha de inicio (puede usarse como referencia para las fechas de los partidos)
     * @return lista de partidos generados (no persistidos)
     */
    List<Partido> generateSchedule(Torneo torneo, List<Equipo> equipos, LocalDate startDate);

    /**
     * Genera el calendario agrupado por jornadas/rondas.
     * La implementación por defecto agrupa todo en una sola lista.
     *
     * @return lista de grupos; cada grupo representa una jornada/ronda.
     */
    default List<List<Partido>> generateGroupedSchedule(Torneo torneo, List<Equipo> equipos, LocalDate startDate) {
        return java.util.Collections.singletonList(generateSchedule(torneo, equipos, startDate));
    }
}


package dev.mrraxyer.sportstournamentmanager.strategies;

import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.models.Partido;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Estrategia Round-Robin (liga). Genera todos los pares únicos (opcional doble vuelta).
 */
public class RoundRobinScheduleStrategy implements MatchScheduleStrategy {

    private final boolean dobleVuelta;

    public RoundRobinScheduleStrategy() {
        this(false);
    }

    public RoundRobinScheduleStrategy(boolean dobleVuelta) {
        this.dobleVuelta = dobleVuelta;
    }

    @Override
    public List<Partido> generateSchedule(Torneo torneo, List<Equipo> equipos, LocalDate startDate) {
        List<Partido> partidos = new ArrayList<>();
        for (List<Partido> jornada : generateGroupedSchedule(torneo, equipos, startDate)) {
            partidos.addAll(jornada);
        }
        return partidos;
    }

    @Override
    public List<List<Partido>> generateGroupedSchedule(Torneo torneo, List<Equipo> equipos, LocalDate startDate) {
        if (equipos == null || equipos.size() < 2) {
            return Collections.emptyList();
        }

        List<Equipo> rotacion = new ArrayList<>(equipos);
        if (rotacion.size() % 2 != 0) {
            rotacion.add(null); // bye
        }

        int totalEquipos = rotacion.size();
        int partidosPorJornada = totalEquipos / 2;
        int rondasBase = totalEquipos - 1;
        List<List<Partido>> jornadas = new ArrayList<>();

        List<Equipo> actual = new LinkedList<>(rotacion);
        for (int ronda = 0; ronda < rondasBase; ronda++) {
            jornadas.add(crearJornada(torneo, actual, startDate.plusDays(ronda).atTime(10, 0), partidosPorJornada));
            actual = rotar(actual);
        }

        if (dobleVuelta) {
            for (int ronda = 0; ronda < rondasBase; ronda++) {
                List<Partido> jornadaVuelta = crearJornadaInvertida(
                        torneo,
                        rotarALaRonda(rotacion, ronda),
                        startDate.plusDays((long) rondasBase + ronda).atTime(10, 0),
                        partidosPorJornada
                );
                jornadas.add(jornadaVuelta);
            }
        }

        return jornadas;
    }

    private List<Partido> crearJornada(Torneo torneo, List<Equipo> actual, LocalDateTime fechaHora, int partidosPorJornada) {
        List<Partido> jornada = new ArrayList<>();
        int size = actual.size();
        int partidoIndex = 0;
        for (int i = 0; i < partidosPorJornada; i++) {
            Equipo local = actual.get(i);
            Equipo visitante = actual.get(size - 1 - i);
            if (local == null || visitante == null) {
                continue;
            }
            Partido partido = new Partido();
            partido.setTorneo(torneo);
            partido.setEquipoLocal(local);
            partido.setEquipoVisitante(visitante);
            partido.setFechaPartido(fechaHora.plusHours(partidoIndex * 2L));
            jornada.add(partido);
            partidoIndex++;
        }
        return jornada;
    }

    private List<Partido> crearJornadaInvertida(Torneo torneo, List<Equipo> actual, LocalDateTime fechaHora, int partidosPorJornada) {
        List<Partido> jornada = new ArrayList<>();
        int size = actual.size();
        int partidoIndex = 0;
        for (int i = 0; i < partidosPorJornada; i++) {
            Equipo local = actual.get(size - 1 - i);
            Equipo visitante = actual.get(i);
            if (local == null || visitante == null) {
                continue;
            }
            Partido partido = new Partido();
            partido.setTorneo(torneo);
            partido.setEquipoLocal(local);
            partido.setEquipoVisitante(visitante);
            partido.setFechaPartido(fechaHora.plusHours(partidoIndex * 2L));
            jornada.add(partido);
            partidoIndex++;
        }
        return jornada;
    }

    private List<Equipo> rotar(List<Equipo> actual) {
        if (actual.size() <= 2) {
            return new ArrayList<>(actual);
        }
        List<Equipo> siguiente = new ArrayList<>(actual.size());
        siguiente.add(actual.get(0));
        siguiente.add(actual.get(actual.size() - 1));
        for (int i = 1; i < actual.size() - 1; i++) {
            siguiente.add(actual.get(i));
        }
        return siguiente;
    }

    private List<Equipo> rotarALaRonda(List<Equipo> base, int ronda) {
        List<Equipo> actual = new ArrayList<>(base);
        if (actual.size() % 2 != 0) {
            actual.add(null);
        }
        for (int i = 0; i < ronda; i++) {
            actual = rotar(actual);
        }
        return actual;
    }
}


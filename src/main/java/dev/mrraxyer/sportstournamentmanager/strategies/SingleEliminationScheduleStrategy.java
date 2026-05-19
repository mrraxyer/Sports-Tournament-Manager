package dev.mrraxyer.sportstournamentmanager.strategies;

import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.models.Partido;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estrategia de Eliminación Directa (playoff).
 */
public class SingleEliminationScheduleStrategy implements MatchScheduleStrategy {

    @Override
    public List<Partido> generateSchedule(Torneo torneo, List<Equipo> equipos, LocalDate startDate) {
        List<Partido> partidos = new ArrayList<>();
        for (List<Partido> ronda : generateGroupedSchedule(torneo, equipos, startDate)) {
            partidos.addAll(ronda);
        }
        return partidos;
    }

    @Override
    public List<List<Partido>> generateGroupedSchedule(Torneo torneo, List<Equipo> equipos, LocalDate startDate) {
        if (equipos == null || equipos.size() < 2) {
            return Collections.emptyList();
        }

        List<Equipo> participantes = new ArrayList<>(equipos);
        int bracketSize = nextPowerOfTwo(participantes.size());
        while (participantes.size() < bracketSize) {
            participantes.add(null);
        }

        List<Integer> order = generateSeedingOrder(bracketSize);
        List<Equipo> ordered = new ArrayList<>(bracketSize);
        for (Integer seed : order) {
            ordered.add(participantes.get(seed - 1));
        }

        List<List<Partido>> bracket = new ArrayList<>();
        List<Equipo> currentRoundParticipants = ordered;
        int roundNumber = 1;
        int globalBracketIndex = 1;

        while (currentRoundParticipants.size() > 1) {
            LocalDateTime roundStart = startDate.atTime(10, 0).plusDays(roundNumber - 1L);
            List<Partido> roundMatches = new ArrayList<>();
            List<Equipo> nextRoundParticipants = new ArrayList<>();
            int matchNumber = 1;
            
            int numMatches = currentRoundParticipants.size() / 2;
            String faseName;
            if (numMatches == 1) faseName = "Final";
            else if (numMatches == 2) faseName = "Semifinal";
            else if (numMatches == 4) faseName = "Cuartos";
            else if (numMatches == 8) faseName = "Octavos";
            else faseName = "Ronda de " + (numMatches * 2);

            for (int i = 0; i < currentRoundParticipants.size(); i += 2) {
                Equipo local = currentRoundParticipants.get(i);
                Equipo visitante = currentRoundParticipants.get(i + 1);

                if (roundNumber == 1) {
                    if (local == null && visitante == null) {
                        continue;
                    }
                    if (local == null) {
                        nextRoundParticipants.add(visitante);
                        continue;
                    }
                    if (visitante == null) {
                        nextRoundParticipants.add(local);
                        continue;
                    }
                }

                Partido partido = new Partido();
                partido.setTorneo(torneo);
                partido.setEquipoLocal(local);
                partido.setEquipoVisitante(visitante);
                partido.setFechaPartido(roundStart.plusHours((matchNumber - 1L) * 2L));
                partido.setFase(faseName);
                partido.setBracketIndex(globalBracketIndex++);
                roundMatches.add(partido);

                nextRoundParticipants.add(null);
                matchNumber++;
            }

            if (!roundMatches.isEmpty()) {
                bracket.add(roundMatches);
            }
            currentRoundParticipants = nextRoundParticipants;
            roundNumber++;
        }

        return bracket;
    }

    private int nextPowerOfTwo(int value) {
        int power = 1;
        while (power < value) {
            power <<= 1;
        }
        return power;
    }

    private List<Integer> generateSeedingOrder(int bracketSize) {
        List<Integer> order = new ArrayList<>();
        order.add(1);
        int size = 1;

        while (size < bracketSize) {
            List<Integer> next = new ArrayList<>(size * 2);
            for (Integer seed : order) {
                next.add(seed);
                next.add(size * 2 + 1 - seed);
            }
            order = next;
            size *= 2;
        }

        return order;
    }

}

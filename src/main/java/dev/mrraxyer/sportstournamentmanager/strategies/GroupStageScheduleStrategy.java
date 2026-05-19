package dev.mrraxyer.sportstournamentmanager.strategies;

import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.models.Partido;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy para generar una fase de grupos: divide equipos en N grupos y aplica
 * round-robin por grupo.
 */
public class GroupStageScheduleStrategy implements MatchScheduleStrategy {

    @Override
    public List<Partido> generateSchedule(Torneo torneo, List<Equipo> equipos, LocalDate startDate) {
        List<Partido> resultado = new ArrayList<>();

        Integer numGrupos = torneo.getNumGrupos();
        if (numGrupos == null || numGrupos <= 0) {
            numGrupos = 2; // valor por defecto
        }

        List<List<Equipo>> grupos = new ArrayList<>();
        for (int i = 0; i < numGrupos; i++)
            grupos.add(new ArrayList<>());

        // repartir por orden de inserción
        for (int i = 0; i < equipos.size(); i++) {
            grupos.get(i % numGrupos).add(equipos.get(i));
        }

        RoundRobinScheduleStrategy rr = new RoundRobinScheduleStrategy();
        char letra = 'A';
        for (List<Equipo> grupo : grupos) {
            if (grupo.isEmpty()) {
                letra++;
                continue;
            }
            List<Partido> partidosGrupo = rr.generateSchedule(torneo, grupo, startDate);
            String codigoGrupo = String.valueOf(letra);
            for (Partido p : partidosGrupo) {
                p.setGrupo(codigoGrupo);
            }
            resultado.addAll(partidosGrupo);
            letra++;
        }

        return resultado;
    }
}

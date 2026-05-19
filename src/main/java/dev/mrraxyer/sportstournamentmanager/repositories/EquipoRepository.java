package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import java.util.List;

/** Repositorio de Equipo. */
public interface EquipoRepository extends BaseRepository<Equipo, Integer> {

    List<Equipo> findByTorneo(Torneo torneo);

    List<Equipo> findByNombreContainingIgnoreCase(String nombre);
}


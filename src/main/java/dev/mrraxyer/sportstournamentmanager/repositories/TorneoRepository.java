package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import java.util.List;

/** Repositorio de Torneo. */
public interface TorneoRepository extends BaseRepository<Torneo, Integer> {

    List<Torneo> findByNombreContainingIgnoreCase(String nombre);

    List<Torneo> findByTipoFormato(String tipoFormato);
}


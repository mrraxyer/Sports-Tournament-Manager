package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.TablaPosiciones;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import java.util.List;
import java.util.Optional;

/** Repositorio de TablaPosiciones. */
public interface TablaPosicionesRepository extends BaseRepository<TablaPosiciones, Integer> {

    List<TablaPosiciones> findByTorneoOrderByPuntosDescGolesAFavorDescGolesEnContraAsc(Torneo torneo);

    Optional<TablaPosiciones> findByTorneoAndEquipo(Torneo torneo, Equipo equipo);

    List<TablaPosiciones> findByTorneo(Torneo torneo);

    List<TablaPosiciones> findByTorneoAndGrupo(Torneo torneo, String grupo);
}

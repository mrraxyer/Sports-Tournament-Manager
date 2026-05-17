package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.TablaPosiciones;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad TablaPosiciones.
 * Hereda todas las operaciones CRUD genéricas de BaseRepository<T, ID>.
 *
 * Incluye métodos específicos de TablaPosiciones para búsquedas comunes.
 */
public interface TablaPosicionesRepository extends BaseRepository<TablaPosiciones, Integer> {

    /**
     * Busca la tabla de posiciones de un torneo.
     * Útil para obtener clasificación ordenada.
     *
     * @param torneo el torneo
     * @return lista de posiciones ordenadas por puntos (descendente) y diferencia goles
     */
    List<TablaPosiciones> findByTorneoOrderByPuntosDescGolesFavorDescGolesContraAsc(Torneo torneo);

    /**
     * Busca la posición de un equipo en un torneo específico.
     *
     * @param torneo el torneo
     * @param equipo el equipo
     * @return Optional con la posición si existe
     */
    Optional<TablaPosiciones> findByTorneoAndEquipo(Torneo torneo, Equipo equipo);

    /**
     * Busca todas las posiciones de un torneo.
     *
     * @param torneo el torneo
     * @return lista de todas las posiciones del torneo
     */
    List<TablaPosiciones> findByTorneo(Torneo torneo);
}


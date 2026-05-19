package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.Partido;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio para la entidad Partido.
 * Hereda todas las operaciones CRUD genéricas de BaseRepository<T, ID>.
 *
 * Incluye métodos específicos de Partido para búsquedas comunes.
 */
public interface PartidoRepository extends BaseRepository<Partido, Integer> {

    /**
     * Busca partidos por torneo.
     * Útil para obtener el calendario de un torneo.
     *
     * @param torneo el torneo
     * @return lista de partidos del torneo
     */
    List<Partido> findByTorneo(Torneo torneo);

    /**
     * Busca partidos en los que participa un equipo (local o visitante).
     * Útil para historial de partidos de un equipo.
     *
     * @param equipo el equipo
     * @return lista de partidos del equipo
     */
    List<Partido> findByEquipoLocalOrEquipoVisitante(Equipo equipoLocal, Equipo equipoVisitante);

    /**
     * Busca partidos de un equipo como local.
     *
     * @param equipo el equipo
     * @return lista de partidos como local
     */
    List<Partido> findByEquipoLocal(Equipo equipo);

    /**
     * Busca partidos de un equipo como visitante.
     *
     * @param equipo el equipo
     * @return lista de partidos como visitante
     */
    List<Partido> findByEquipoVisitante(Equipo equipo);

    /**
     * Busca los partidos cara a cara (head-to-head) entre dos equipos en un mismo
     * torneo.
     */
    @Query("SELECT p FROM Partido p WHERE p.torneo = :torneo AND ((p.equipoLocal = :a AND p.equipoVisitante = :b) OR (p.equipoLocal = :b AND p.equipoVisitante = :a))")
    List<Partido> findHeadToHead(@Param("torneo") Torneo torneo, @Param("a") Equipo equipoA,
            @Param("b") Equipo equipoB);
}

package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.Partido;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

/** Repositorio de Partido. */
public interface PartidoRepository extends BaseRepository<Partido, Integer> {

    List<Partido> findByTorneo(Torneo torneo);

    List<Partido> findByEquipoLocalOrEquipoVisitante(Equipo equipoLocal, Equipo equipoVisitante);

    List<Partido> findByEquipoLocal(Equipo equipo);

    List<Partido> findByEquipoVisitante(Equipo equipo);

    /** Partidos cara a cara entre dos equipos en el mismo torneo. */
    @Query("SELECT p FROM Partido p WHERE p.torneo = :torneo AND ((p.equipoLocal = :a AND p.equipoVisitante = :b) OR (p.equipoLocal = :b AND p.equipoVisitante = :a))")
    List<Partido> findHeadToHead(@Param("torneo") Torneo torneo, @Param("a") Equipo equipoA,
            @Param("b") Equipo equipoB);

    /** Próximos partidos con plaza local o visitante vacía, ordenados por fecha. */
    @Query("SELECT p FROM Partido p WHERE p.torneo = :torneo AND p.fechaPartido > :fecha AND (p.equipoLocal IS NULL OR p.equipoVisitante IS NULL) ORDER BY p.fechaPartido ASC")
    List<Partido> findNextMatchesWithEmptySlot(@Param("torneo") Torneo torneo, @Param("fecha") LocalDateTime fecha);
}

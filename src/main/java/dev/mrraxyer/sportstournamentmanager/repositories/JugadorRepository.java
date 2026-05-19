package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.Jugador;
import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import java.util.List;

/** Repositorio de Jugador. */
public interface JugadorRepository extends BaseRepository<Jugador, Integer> {

    List<Jugador> findByEquipo(Equipo equipo);

    List<Jugador> findByNombreContainingIgnoreCase(String nombre);
}


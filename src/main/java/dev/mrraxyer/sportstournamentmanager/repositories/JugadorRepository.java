package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.Jugador;
import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import java.util.List;

/**
 * Repositorio para la entidad Jugador.
 * Hereda todas las operaciones CRUD genéricas de BaseRepository<T, ID>.
 *
 * Incluye métodos específicos de Jugador para búsquedas comunes.
 */
public interface JugadorRepository extends BaseRepository<Jugador, Integer> {
    
    /**
     * Busca jugadores por equipo.
     * Útil para obtener el plantel de un equipo.
     *
     * @param equipo el equipo
     * @return lista de jugadores del equipo
     */
    List<Jugador> findByEquipo(Equipo equipo);
    
    /**
     * Busca jugadores por nombre.
     * Útil para búsquedas y listados.
     *
     * @param nombre el nombre del jugador (búsqueda parcial)
     * @return lista de jugadores que contienen el nombre
     */
    List<Jugador> findByNombreContainingIgnoreCase(String nombre);
}


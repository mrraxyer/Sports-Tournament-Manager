package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import java.util.List;

/**
 * Repositorio para la entidad Equipo.
 * Hereda todas las operaciones CRUD genéricas de BaseRepository<T, ID>.
 *
 * Incluye métodos específicos de Equipo para búsquedas comunes.
 */
public interface EquipoRepository extends BaseRepository<Equipo, Integer> {
    
    /**
     * Busca equipos por torneo.
     * Útil para obtener todos los equipos participantes en un torneo.
     *
     * @param torneo el torneo
     * @return lista de equipos del torneo
     */
    List<Equipo> findByTorneo(Torneo torneo);
    
    /**
     * Busca equipos por nombre.
     * Útil para búsquedas y validación de nombres únicos.
     *
     * @param nombre el nombre del equipo (búsqueda parcial)
     * @return lista de equipos que contienen el nombre
     */
    List<Equipo> findByNombreContainingIgnoreCase(String nombre);
}


package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import java.util.List;

/**
 * Repositorio para la entidad Torneo.
 * Hereda todas las operaciones CRUD genéricas de BaseRepository<T, ID>.
 *
 * Incluye métodos específicos de Torneo para búsquedas comunes.
 */
public interface TorneoRepository extends BaseRepository<Torneo, Integer> {
    
    /**
     * Busca torneos por nombre.
     * Útil para búsquedas y filtrado en listados.
     *
     * @param nombre el nombre del torneo (búsqueda parcial)
     * @return lista de torneos que contienen el nombre
     */
    List<Torneo> findByNombreContainingIgnoreCase(String nombre);
    
    /**
     * Busca torneos por tipo de formato.
     *
     * @param tipoFormato el tipo de formato (ej: "Liga", "Playoff")
     * @return lista de torneos con ese formato
     */
    List<Torneo> findByTipoFormato(String tipoFormato);
}


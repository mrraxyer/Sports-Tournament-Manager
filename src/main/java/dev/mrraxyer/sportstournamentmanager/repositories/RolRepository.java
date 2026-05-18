package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.Rol;
import java.util.Optional;

/**
 * Repositorio para la entidad Rol.
 * Hereda todas las operaciones CRUD genéricas de BaseRepository<T, ID>.
 *
 */
public interface RolRepository extends BaseRepository<Rol, Integer> {
    /**
     * Busca un rol por su nombre.
     *
     * @param nombre el nombre del rol
     * @return Optional con el rol si existe
     */
    Optional<Rol> findByNombre(String nombre);
}


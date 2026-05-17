package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.Usuario;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario.
 * Hereda todas las operaciones CRUD genéricas de BaseRepository<T, ID>.
 *
 * Incluye métodos específicos de Usuario para búsquedas comunes.
 */
public interface UsuarioRepository extends BaseRepository<Usuario, Integer> {
    
    /**
     * Busca un usuario por su correo electrónico.
     * Útil para validación de login y búsqueda de usuarios únicos.
     *
     * @param correo el correo electrónico del usuario
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> findByCorreo(String correo);
}


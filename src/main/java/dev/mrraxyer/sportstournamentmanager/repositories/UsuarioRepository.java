package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.Usuario;
import java.util.Optional;

/** Repositorio de Usuario. */
public interface UsuarioRepository extends BaseRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreo(String correo);
}


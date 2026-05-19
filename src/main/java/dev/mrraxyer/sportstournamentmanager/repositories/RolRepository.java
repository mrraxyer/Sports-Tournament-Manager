package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.Rol;
import java.util.Optional;

/** Repositorio de Rol. */
public interface RolRepository extends BaseRepository<Rol, Integer> {

    Optional<Rol> findByNombre(String nombre);
}


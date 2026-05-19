package dev.mrraxyer.sportstournamentmanager.repositories;

import dev.mrraxyer.sportstournamentmanager.models.GlobalProperty;
import java.util.Optional;

/**
 * Repositorio para las propiedades globales
 */
public interface GlobalPropertyRepository extends BaseRepository<GlobalProperty, Integer> {
    Optional<GlobalProperty> findByKey(String key);
}


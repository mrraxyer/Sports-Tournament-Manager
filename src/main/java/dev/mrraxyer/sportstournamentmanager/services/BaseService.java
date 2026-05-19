package dev.mrraxyer.sportstournamentmanager.services;

import dev.mrraxyer.sportstournamentmanager.repositories.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * Servicio Base Genérico<T, ID>
 *
 * @param <T>  Tipo de la entidad
 * @param <ID> Tipo del identificador
 */
public abstract class BaseService<T, ID> {

    protected abstract BaseRepository<T, ID> getRepository();

    /**
     * Guarda una entidad (insert o update)
     */
    public T save(T entity) {
        return getRepository().save(entity);
    }

    /**
     * Obtiene una entidad por ID
     */
    public Optional<T> findById(ID id) {
        return getRepository().findById(id);
    }

    /**
     * Obtiene todas las entidades
     */
    public List<T> findAll() {
        return getRepository().findAll();
    }

    /**
     * Obtiene todas las entidades paginadas
     */
    public Page<T> findAll(Pageable pageable) {
        return getRepository().findAll(pageable);
    }

    /**
     * Verifica si existe una entidad por ID
     */
    public boolean existsById(ID id) {
        return getRepository().existsById(id);
    }

    /**
     * Elimina una entidad por ID
     */
    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }

    /**
     * Elimina una entidad
     */
    public void delete(T entity) {
        getRepository().delete(entity);
    }

    /**
     * Obtiene el total de entidades
     */
    public long count() {
        return getRepository().count();
    }

    /**
     * Guarda múltiples entidades (batch)
     */
    public List<T> saveAll(List<T> entities) {
        return getRepository().saveAll(entities);
    }
}


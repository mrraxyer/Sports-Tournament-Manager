package dev.mrraxyer.sportstournamentmanager.services;

import dev.mrraxyer.sportstournamentmanager.repositories.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/** Servicio genérico base con operaciones CRUD. */
public abstract class BaseService<T, ID> {

    protected abstract BaseRepository<T, ID> getRepository();

    public T save(T entity) {
        return getRepository().save(entity);
    }

    public Optional<T> findById(ID id) {
        return getRepository().findById(id);
    }

    public List<T> findAll() {
        return getRepository().findAll();
    }

    public Page<T> findAll(Pageable pageable) {
        return getRepository().findAll(pageable);
    }

    public boolean existsById(ID id) {
        return getRepository().existsById(id);
    }

    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }

    public void delete(T entity) {
        getRepository().delete(entity);
    }

    public long count() {
        return getRepository().count();
    }

    public List<T> saveAll(List<T> entities) {
        return getRepository().saveAll(entities);
    }
}


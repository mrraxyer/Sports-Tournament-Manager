package dev.mrraxyer.sportstournamentmanager.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/** Repositorio genérico base. */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    boolean existsById(ID id);

    Optional<T> findById(ID id);

    List<T> findAll();

    <S extends T> S save(S entity);

    void deleteById(ID id);

    void delete(T entity);

    long count();
}


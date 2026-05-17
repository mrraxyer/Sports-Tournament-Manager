package dev.mrraxyer.sportstournamentmanager.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz Genérica BaseRepository<T, ID>
 *
 * @param <T>  Tipo de la entidad (Usuario, Torneo, Equipo, etc.)
 * @param <ID> Tipo del identificador único (Integer, Long, String, etc.)
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    
    /**
     * Verifica si existe una entidad con el identificador especificado.
     *
     * @param id el identificador a buscar
     * @return true si existe, false en caso contrario
     */
    boolean existsById(ID id);
    
    /**
     * Obtiene una entidad por su identificador.
     *
     * @param id el identificador a buscar
     * @return Optional con la entidad si existe
     */
    Optional<T> findById(ID id);
    
    /**
     * Obtiene todas las entidades.
     *
     * @return lista de todas las entidades
     */
    List<T> findAll();
    
    /**
     * Guarda una entidad (insert o update).
     *
     * @param entity la entidad a guardar
     * @return la entidad guardada
     */
    <S extends T> S save(S entity);
    
    /**
     * Elimina una entidad por su identificador.
     *
     * @param id el identificador de la entidad a eliminar
     */
    void deleteById(ID id);
    
    /**
     * Elimina una entidad específica.
     *
     * @param entity la entidad a eliminar
     */
    void delete(T entity);
    
    /**
     * Obtiene el número total de entidades.
     *
     * @return cantidad de entidades
     */
    long count();
}


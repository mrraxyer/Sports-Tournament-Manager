package dev.mrraxyer.sportstournamentmanager.services.impl;

import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.repositories.BaseRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.TorneoRepository;
import dev.mrraxyer.sportstournamentmanager.services.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de Torneo
 *
 * Extiende BaseService<Torneo, Integer> para herdar todas las operaciones CRUD genéricas.
 *
 */
@Service
public class TorneoService extends BaseService<Torneo, Integer> {
    
    @Autowired
    private TorneoRepository torneoRepository;
    
    @Override
    protected BaseRepository<Torneo, Integer> getRepository() {
        return torneoRepository;
    }
    
    /**
     * Busca torneos por nombre
     * Método específico que extiende la funcionalidad base
     */
    public List<Torneo> findByNombre(String nombre) {
        return torneoRepository.findByNombreContainingIgnoreCase(nombre);
    }
    
    /**
     * Busca torneos por tipo de formato
     * Método específico que extiende la funcionalidad base
     */
    public List<Torneo> findByTipoFormato(String tipoFormato) {
        return torneoRepository.findByTipoFormato(tipoFormato);
    }
}


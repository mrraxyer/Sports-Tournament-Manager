package dev.mrraxyer.sportstournamentmanager.services.impl;

import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.repositories.BaseRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.EquipoRepository;
import dev.mrraxyer.sportstournamentmanager.services.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de Equipo
 *
 * Extiende BaseService<Equipo, Integer> para herdar todas las operaciones CRUD genéricas.
 */
@Service
public class EquipoService extends BaseService<Equipo, Integer> {

    @Autowired
    private EquipoRepository equipoRepository;

    @Override
    protected BaseRepository<Equipo, Integer> getRepository() {
        return equipoRepository;
    }

    /**
     * Busca equipos por torneo
     * Método específico que extiende la funcionalidad base
     */
    public List<Equipo> findByTorneo(Torneo torneo) {
        return equipoRepository.findByTorneo(torneo);
    }

    /**
     * Busca equipos por nombre
     * Método específico que extiende la funcionalidad base
     */
    public List<Equipo> findByNombre(String nombre) {
        return equipoRepository.findByNombreContainingIgnoreCase(nombre);
    }
}


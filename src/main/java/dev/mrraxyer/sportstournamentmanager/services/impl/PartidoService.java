package dev.mrraxyer.sportstournamentmanager.services.impl;

import dev.mrraxyer.sportstournamentmanager.events.PartidoResultadoEvent;
import dev.mrraxyer.sportstournamentmanager.models.Partido;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.repositories.BaseRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.PartidoRepository;
import dev.mrraxyer.sportstournamentmanager.services.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

import java.util.List;

/**
 * Servicio de Partido
 *
 * Extiende BaseService<Partido, Integer> para herdar todas las operaciones CRUD
 * genéricas.
 * Adiciona funcionalidad de publicación de eventos cuando se registra o
 * actualiza
 * el resultado de un partido, habilitando patrones reactivos.
 */
@Service
public class PartidoService extends BaseService<Partido, Integer> {

    @Autowired
    private PartidoRepository partidoRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    protected BaseRepository<Partido, Integer> getRepository() {
        return partidoRepository;
    }

    /**
     * Guarda un partido y publica un evento si tiene resultados
     * El evento será escuchado por observadores como TablaPosicionesService
     *
     * @param partido el partido a guardar
     * @return el partido guardado
     */
    @Override
    public Partido save(Partido partido) {
        Partido partidoGuardado = super.save(partido);

        // Publicar evento sólo cuando el partido haya sido marcado como jugado
        // y tenga ambos equipos y torneo definido. Además incluye el campo grupo.
        if (Boolean.TRUE.equals(partidoGuardado.getJugado())
                && partidoGuardado.getEquipoLocal() != null
                && partidoGuardado.getEquipoVisitante() != null
                && partidoGuardado.getTorneo() != null) {

            PartidoResultadoEvent evento = new PartidoResultadoEvent(
                    this,
                    partidoGuardado.getPartidosId(),
                    partidoGuardado.getTorneo().getTorneosId(),
                    partidoGuardado.getEquipoLocal().getEquiposId(),
                    partidoGuardado.getEquipoVisitante().getEquiposId(),
                    partidoGuardado.getGolesLocal(),
                    partidoGuardado.getGolesVisitante(),
                    partidoGuardado.getGrupo());

            eventPublisher.publishEvent(evento);
            // Intento de asignar el ganador al siguiente partido disponible (si existe)
            try {
                Equipo ganador = null;
                if (partidoGuardado.getGolesLocal() != null && partidoGuardado.getGolesVisitante() != null) {
                    if (partidoGuardado.getGolesLocal() > partidoGuardado.getGolesVisitante()) {
                        ganador = partidoGuardado.getEquipoLocal();
                    } else if (partidoGuardado.getGolesVisitante() > partidoGuardado.getGolesLocal()) {
                        ganador = partidoGuardado.getEquipoVisitante();
                    }
                }

                if (ganador != null) {
                    // Buscar próximos partidos con huecos
                    List<Partido> siguientes = partidoRepository.findNextMatchesWithEmptySlot(
                            partidoGuardado.getTorneo(), partidoGuardado.getFechaPartido());
                    if (siguientes != null && !siguientes.isEmpty()) {
                        for (Partido siguiente : siguientes) {
                            // Evitar duplicar si el equipo ya está asignado
                            if (siguiente.getEquipoLocal() != null
                                    && siguiente.getEquipoLocal().getEquiposId().equals(ganador.getEquiposId()))
                                continue;
                            if (siguiente.getEquipoVisitante() != null
                                    && siguiente.getEquipoVisitante().getEquiposId().equals(ganador.getEquiposId()))
                                continue;

                            boolean assigned = false;
                            if (siguiente.getEquipoLocal() == null) {
                                siguiente.setEquipoLocal(ganador);
                                assigned = true;
                            } else if (siguiente.getEquipoVisitante() == null) {
                                siguiente.setEquipoVisitante(ganador);
                                assigned = true;
                            }

                            if (assigned) {
                                // Guardar el partido siguiente con el equipo asignado
                                super.save(siguiente);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                // No bloquear el guardado principal si la asignación falla
            }
        }

        return partidoGuardado;
    }

    /**
     * Busca partidos por torneo
     * Útil para obtener el calendario de un torneo
     *
     * @param torneo el torneo
     * @return lista de partidos del torneo
     */
    public List<Partido> findByTorneo(Torneo torneo) {
        return partidoRepository.findByTorneo(torneo);
    }
}

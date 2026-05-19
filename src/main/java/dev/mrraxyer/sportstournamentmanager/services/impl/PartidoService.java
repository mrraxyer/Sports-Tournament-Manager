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

/** Servicio de Partido. Publica eventos al registrar resultados. */
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

    /** Guarda el partido y publica evento de resultado si está marcado como jugado. */
    @Override
    public Partido save(Partido partido) {
        Partido partidoGuardado = super.save(partido);

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
            // Asignar ganador al siguiente partido del bracket (eliminación directa)
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
                    List<Partido> siguientes = partidoRepository.findNextMatchesWithEmptySlot(
                            partidoGuardado.getTorneo(), partidoGuardado.getFechaPartido());
                    if (siguientes != null && !siguientes.isEmpty()) {
                        for (Partido siguiente : siguientes) {
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

    public List<Partido> findByTorneo(Torneo torneo) {
        return partidoRepository.findByTorneo(torneo);
    }
}

package dev.mrraxyer.sportstournamentmanager.services.impl;

import dev.mrraxyer.sportstournamentmanager.events.PartidoResultadoEvent;
import dev.mrraxyer.sportstournamentmanager.models.Partido;
import dev.mrraxyer.sportstournamentmanager.repositories.BaseRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.PartidoRepository;
import dev.mrraxyer.sportstournamentmanager.services.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de Partido
 *
 * Extiende BaseService<Partido, Integer> para herdar todas las operaciones CRUD genéricas.
 * Adiciona funcionalidad de publicación de eventos cuando se registra o actualiza
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

        // Publicar evento cuando se guarden/actualicen los resultados
        // Solo publica si el partido tiene ambos equipos y es del mismo torneo
        if (partidoGuardado.getEquipoLocal() != null &&
            partidoGuardado.getEquipoVisitante() != null &&
            partidoGuardado.getTorneo() != null) {

            PartidoResultadoEvent evento = new PartidoResultadoEvent(
                this,
                partidoGuardado.getPartidosId(),
                partidoGuardado.getTorneo().getTorneosId(),
                partidoGuardado.getEquipoLocal().getEquiposId(),
                partidoGuardado.getEquipoVisitante().getEquiposId(),
                partidoGuardado.getGolesLocal(),
                partidoGuardado.getGolesVisitante()
            );

            eventPublisher.publishEvent(evento);
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
    public List<Partido> findByTorneo(Object torneo) {
        // Este método se implementaría si fuera necesario
        return null;
    }
}


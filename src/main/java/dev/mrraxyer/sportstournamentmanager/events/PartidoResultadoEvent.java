package dev.mrraxyer.sportstournamentmanager.events;

import org.springframework.context.ApplicationEvent;

/**
 * Evento publicado cuando se registra o actualiza el resultado de un partido.
 * Este evento es escuchado por observadores como el servicio de tabla de posiciones,
 * permitiendo la actualización reactiva de estadísticas.
 */
public class PartidoResultadoEvent extends ApplicationEvent {

    private final Integer partidoId;
    private final Integer torneoId;
    private final Integer equipoLocalId;
    private final Integer equipoVisitanteId;
    private final Integer golesLocal;
    private final Integer golesVisitante;

    /**
     * Crea un nuevo evento de resultado de partido
     *
     * @param source Objeto que publica el evento
     * @param partidoId ID del partido
     * @param torneoId ID del torneo
     * @param equipoLocalId ID del equipo local
     * @param equipoVisitanteId ID del equipo visitante
     * @param golesLocal Goles del equipo local
     * @param golesVisitante Goles del equipo visitante
     */
    public PartidoResultadoEvent(Object source, Integer partidoId, Integer torneoId,
                                 Integer equipoLocalId, Integer equipoVisitanteId,
                                 Integer golesLocal, Integer golesVisitante) {
        super(source);
        this.partidoId = partidoId;
        this.torneoId = torneoId;
        this.equipoLocalId = equipoLocalId;
        this.equipoVisitanteId = equipoVisitanteId;
        this.golesLocal = golesLocal;
        this.golesVisitante = golesVisitante;
    }

    // Getters
    public Integer getPartidoId() {
        return partidoId;
    }

    public Integer getTorneoId() {
        return torneoId;
    }

    public Integer getEquipoLocalId() {
        return equipoLocalId;
    }

    public Integer getEquipoVisitanteId() {
        return equipoVisitanteId;
    }

    public Integer getGolesLocal() {
        return golesLocal;
    }

    public Integer getGolesVisitante() {
        return golesVisitante;
    }

    @Override
    public String toString() {
        return "PartidoResultadoEvent{" +
                "partidoId=" + partidoId +
                ", torneoId=" + torneoId +
                ", equipoLocalId=" + equipoLocalId +
                ", equipoVisitanteId=" + equipoVisitanteId +
                ", golesLocal=" + golesLocal +
                ", golesVisitante=" + golesVisitante +
                '}';
    }
}


package dev.mrraxyer.sportstournamentmanager.events;

import org.springframework.context.ApplicationEvent;

/** Evento de resultado de partido. Escuchado por TablaPosicionesService para actualizar estadísticas. */
public class PartidoResultadoEvent extends ApplicationEvent {

    private final Integer partidoId;
    private final Integer torneoId;
    private final Integer equipoLocalId;
    private final Integer equipoVisitanteId;
    private final Integer golesLocal;
    private final Integer golesVisitante;
    private final String grupo;

    public PartidoResultadoEvent(Object source, Integer partidoId, Integer torneoId,
            Integer equipoLocalId, Integer equipoVisitanteId,
            Integer golesLocal, Integer golesVisitante,
            String grupo) {
        super(source);
        this.partidoId = partidoId;
        this.torneoId = torneoId;
        this.equipoLocalId = equipoLocalId;
        this.equipoVisitanteId = equipoVisitanteId;
        this.golesLocal = golesLocal;
        this.golesVisitante = golesVisitante;
        this.grupo = grupo;
    }

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

    public String getGrupo() {
        return grupo;
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
                ", grupo=" + grupo +
                '}';
    }
}

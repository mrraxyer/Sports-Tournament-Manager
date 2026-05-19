package dev.mrraxyer.sportstournamentmanager.services.impl;

import dev.mrraxyer.sportstournamentmanager.events.PartidoResultadoEvent;
import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.models.TablaPosiciones;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.repositories.BaseRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.EquipoRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.TablaPosicionesRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.TorneoRepository;
import dev.mrraxyer.sportstournamentmanager.services.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Servicio de Tabla de Posiciones. Actualiza estadísticas reactivamente vía @EventListener. */
@Service
public class TablaPosicionesService extends BaseService<TablaPosiciones, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(TablaPosicionesService.class);

    @Autowired
    private TablaPosicionesRepository tablaPosicionesRepository;

    @Autowired
    private TorneoRepository torneoRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Override
    protected BaseRepository<TablaPosiciones, Integer> getRepository() {
        return tablaPosicionesRepository;
    }

    /** Actualiza la tabla de posiciones al recibir un evento de resultado de partido. */
    @EventListener
    @Transactional
    public void onPartidoResultado(PartidoResultadoEvent evento) {
        logger.info("Evento de resultado de partido recibido: {}", evento);

        try {
            Integer torneoId = evento.getTorneoId();
            Integer equipoLocalId = evento.getEquipoLocalId();
            Integer equipoVisitanteId = evento.getEquipoVisitanteId();
            Integer golesLocal = evento.getGolesLocal();
            Integer golesVisitante = evento.getGolesVisitante();

            if (torneoId == null || equipoLocalId == null || equipoVisitanteId == null) {
                logger.warn("Evento de partido con datos incompletos: {}", evento);
                return;
            }

            Optional<Torneo> torneoOpt = torneoRepository.findById(torneoId);
            if (!torneoOpt.isPresent()) {
                logger.warn("Torneo no encontrado con ID: {}", torneoId);
                return;
            }
            Torneo torneo = torneoOpt.get();

            Optional<Equipo> equipoLocalOpt = equipoRepository.findById(equipoLocalId);
            Optional<Equipo> equipoVisitanteOpt = equipoRepository.findById(equipoVisitanteId);

            if (!equipoLocalOpt.isPresent() || !equipoVisitanteOpt.isPresent()) {
                logger.warn("Uno o más equipos no encontrados. Local ID: {}, Visitante ID: {}",
                        equipoLocalId, equipoVisitanteId);
                return;
            }

            Equipo equipoLocal = equipoLocalOpt.get();
            Equipo equipoVisitante = equipoVisitanteOpt.get();

            String grupo = evento.getGrupo();

            actualizarEstadisticasEquipo(torneo, equipoLocal, golesLocal, golesVisitante, grupo);
            actualizarEstadisticasEquipo(torneo, equipoVisitante, golesVisitante, golesLocal, grupo);

            logger.info("Estadísticas actualizadas exitosamente para el partido de {} vs {}",
                    equipoLocal.getNombre(), equipoVisitante.getNombre());

        } catch (Exception e) {
            logger.error("Error procesando evento de resultado de partido: {}", evento, e);
            throw new RuntimeException("Error al actualizar tabla de posiciones", e);
        }
    }

    /** Actualiza estadísticas del equipo en la tabla de posiciones. Crea el registro si no existe. */
    private void actualizarEstadisticasEquipo(Torneo torneo, Equipo equipo,
            Integer golesAFavor, Integer golesEnContra,
            String grupo) {

        Optional<TablaPosiciones> tablaPosOpt = tablaPosicionesRepository.findByTorneoAndEquipo(torneo, equipo);

        TablaPosiciones tablaPosiciones;
        if (tablaPosOpt.isPresent()) {
            tablaPosiciones = tablaPosOpt.get();
        } else {
            tablaPosiciones = new TablaPosiciones();
            tablaPosiciones.setTorneo(torneo);
            tablaPosiciones.setEquipo(equipo);
            tablaPosiciones.setPartidosJugados(0);
            tablaPosiciones.setPuntos(0);
            tablaPosiciones.setGolesAFavor(0);
            tablaPosiciones.setGolesEnContra(0);
            tablaPosiciones.setGrupo(grupo);
        }

        tablaPosiciones.setPartidosJugados(tablaPosiciones.getPartidosJugados() + 1);
        tablaPosiciones.setGolesAFavor(tablaPosiciones.getGolesAFavor() + golesAFavor);
        tablaPosiciones.setGolesEnContra(tablaPosiciones.getGolesEnContra() + golesEnContra);

        int puntosAñadidos = calcularPuntos(golesAFavor, golesEnContra);
        tablaPosiciones.setPuntos(tablaPosiciones.getPuntos() + puntosAñadidos);

        if (golesAFavor > golesEnContra) {
            tablaPosiciones.setVictorias(tablaPosiciones.getVictorias() + 1);
        } else if (golesAFavor.equals(golesEnContra)) {
            tablaPosiciones.setEmpates(tablaPosiciones.getEmpates() + 1);
        } else {
            tablaPosiciones.setDerrotas(tablaPosiciones.getDerrotas() + 1);
        }

        tablaPosicionesRepository.save(tablaPosiciones);

        logger.debug("Estadísticas de {} actualizadas: {} partidos, {} puntos, {}-{} goles",
                equipo.getNombre(),
                tablaPosiciones.getPartidosJugados(),
                tablaPosiciones.getPuntos(),
                tablaPosiciones.getGolesAFavor(),
                tablaPosiciones.getGolesEnContra());
    }

    /** Retorna puntos: 3 victoria, 1 empate, 0 derrota. */
    private int calcularPuntos(Integer golesAFavor, Integer golesEnContra) {
        if (golesAFavor > golesEnContra) {
            return 3;
        } else if (golesAFavor.equals(golesEnContra)) {
            return 1;
        } else {
            return 0;
        }
    }

    public java.util.List<TablaPosiciones> findByTorneo(Integer torneosId) {
        Optional<Torneo> torneoOpt = torneoRepository.findById(torneosId);
        if (!torneoOpt.isPresent()) {
            return new java.util.ArrayList<>();
        }
        return tablaPosicionesRepository.findByTorneo(torneoOpt.get());
    }
}

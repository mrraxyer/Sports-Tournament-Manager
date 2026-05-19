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

/**
 * Servicio de Tabla de Posiciones
 *
 * Extiende BaseService<TablaPosiciones, Integer> para herdar todas las
 * operaciones CRUD genéricas.
 * Implementa el patrón Observador mediante @EventListener para escuchar eventos
 * de resultados de partidos
 * y hace automática la actualización reactiva de estadísticas.
 */
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

    /**
     * Listener que escucha eventos de resultado de partido.
     * Cuando se publica un PartidoResultadoEvent, este método es invocado
     * automáticamente
     * para actualizar reactivamente la tabla de posiciones.
     *
     * @param evento el evento publicado
     */
    @EventListener
    @Transactional
    public void onPartidoResultado(PartidoResultadoEvent evento) {
        logger.info("Evento de resultado de partido recibido: {}", evento);

        try {
            // Obtener datos del evento
            Integer torneoId = evento.getTorneoId();
            Integer equipoLocalId = evento.getEquipoLocalId();
            Integer equipoVisitanteId = evento.getEquipoVisitanteId();
            Integer golesLocal = evento.getGolesLocal();
            Integer golesVisitante = evento.getGolesVisitante();

            // Validar que tenemos datos válidos
            if (torneoId == null || equipoLocalId == null || equipoVisitanteId == null) {
                logger.warn("Evento de partido con datos incompletos: {}", evento);
                return;
            }

            // Obtener torneo
            Optional<Torneo> torneoOpt = torneoRepository.findById(torneoId);
            if (!torneoOpt.isPresent()) {
                logger.warn("Torneo no encontrado con ID: {}", torneoId);
                return;
            }
            Torneo torneo = torneoOpt.get();

            // Obtener equipos
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

            // Actualizar estadísticas del equipo local
            actualizarEstadisticasEquipo(torneo, equipoLocal, golesLocal, golesVisitante, grupo);

            // Actualizar estadísticas del equipo visitante
            actualizarEstadisticasEquipo(torneo, equipoVisitante, golesVisitante, golesLocal, grupo);

            logger.info("Estadísticas actualizadas exitosamente para el partido de {} vs {}",
                    equipoLocal.getNombre(), equipoVisitante.getNombre());

        } catch (Exception e) {
            logger.error("Error procesando evento de resultado de partido: {}", evento, e);
            throw new RuntimeException("Error al actualizar tabla de posiciones", e);
        }
    }

    /**
     * Actualiza las estadísticas de un equipo para un torneo específico
     * Recalcula puntos basado en el resultado (3-victoria, 1-empate, 0-derrota)
     *
     * @param torneo        el torneo
     * @param equipo        el equipo
     * @param golesAFavor   goles marcados por el equipo
     * @param golesEnContra goles recibidos por el equipo
     */
    private void actualizarEstadisticasEquipo(Torneo torneo, Equipo equipo,
            Integer golesAFavor, Integer golesEnContra,
            String grupo) {

        // Obtener o crear la entrada en tabla de posiciones
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
            // asignar grupo si viene del evento (útil para fase de grupos)
            tablaPosiciones.setGrupo(grupo);
        }

        // Incrementar partidos jugados
        tablaPosiciones.setPartidosJugados(tablaPosiciones.getPartidosJugados() + 1);

        // Actualizar goles
        tablaPosiciones.setGolesAFavor(tablaPosiciones.getGolesAFavor() + golesAFavor);
        tablaPosiciones.setGolesEnContra(tablaPosiciones.getGolesEnContra() + golesEnContra);

        // Calcular y actualizar puntos
        int puntosAñadidos = calcularPuntos(golesAFavor, golesEnContra);
        tablaPosiciones.setPuntos(tablaPosiciones.getPuntos() + puntosAñadidos);

        // Actualizar victorias/empates/derrotas
        if (golesAFavor > golesEnContra) {
            tablaPosiciones.setVictorias(tablaPosiciones.getVictorias() + 1);
        } else if (golesAFavor.equals(golesEnContra)) {
            tablaPosiciones.setEmpates(tablaPosiciones.getEmpates() + 1);
        } else {
            tablaPosiciones.setDerrotas(tablaPosiciones.getDerrotas() + 1);
        }

        // Guardar cambios
        tablaPosicionesRepository.save(tablaPosiciones);

        logger.debug("Estadísticas de {} actualizadas: {} partidos, {} puntos, {}-{} goles",
                equipo.getNombre(),
                tablaPosiciones.getPartidosJugados(),
                tablaPosiciones.getPuntos(),
                tablaPosiciones.getGolesAFavor(),
                tablaPosiciones.getGolesEnContra());
    }

    /**
     * Calcula los puntos otorgados basado en el resultado del partido
     * - 3 puntos por victorias
     * - 1 punto por empates
     * - 0 puntos por derrotas
     *
     * @param golesAFavor   goles marcados por el equipo
     * @param golesEnContra goles recibidos por el equipo
     * @return puntos asignados
     */
    private int calcularPuntos(Integer golesAFavor, Integer golesEnContra) {
        if (golesAFavor > golesEnContra) {
            return 3; // Victoria
        } else if (golesAFavor.equals(golesEnContra)) {
            return 1; // Empate
        } else {
            return 0; // Derrota
        }
    }

    /**
     * Obtiene todas las posiciones de un torneo por ID.
     * Útil para consultar la clasificación completa y filtrar equipos calificados.
     *
     * @param torneosId el ID del torneo
     * @return lista de posiciones del torneo
     */
    public java.util.List<TablaPosiciones> findByTorneo(Integer torneosId) {
        Optional<Torneo> torneoOpt = torneoRepository.findById(torneosId);
        if (!torneoOpt.isPresent()) {
            return new java.util.ArrayList<>();
        }
        return tablaPosicionesRepository.findByTorneo(torneoOpt.get());
    }
}

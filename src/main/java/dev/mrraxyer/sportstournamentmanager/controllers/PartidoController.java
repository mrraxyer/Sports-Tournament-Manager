package dev.mrraxyer.sportstournamentmanager.controllers;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.dto.ApiResponseBuilder;
import dev.mrraxyer.sportstournamentmanager.models.Partido;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.repositories.TorneoRepository;
import dev.mrraxyer.sportstournamentmanager.services.impl.PartidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de Partidos
 * Gestiona las operaciones CRUD de partidos y la publicación de eventos
 * cuando se registran o actualizan resultados.
 */
@RestController
@RequestMapping("/api/partidos")
public class PartidoController {

    @Autowired
    private PartidoService partidoService;

    @Autowired
    private TorneoRepository torneoRepository;

    /**
     * Obtiene un partido por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Partido>> obtenerPartido(@PathVariable Integer id) {
        Optional<Partido> partido = partidoService.findById(id);

        if (partido.isPresent()) {
            ApiResponse<Partido> response = ApiResponseBuilder
                    .success(partido.get())
                    .message("Partido encontrado")
                    .path("/api/partidos/" + id)
                    .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Partido> response = ApiResponseBuilder
                    .<Partido>error("Partido no encontrado", HttpStatus.NOT_FOUND.value())
                    .path("/api/partidos/" + id)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Obtiene todos los partidos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Partido>>> listarPartidos() {
        List<Partido> partidos = partidoService.findAll();

        ApiResponse<List<Partido>> response = ApiResponseBuilder
                .success(partidos)
                .message("Total de partidos: " + partidos.size())
                .path("/api/partidos")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todos los partidos de un torneo
     */
    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<ApiResponse<List<Partido>>> listarPartidosPorTorneo(
            @PathVariable Integer torneoId) {

        Optional<Torneo> torneoOpt = torneoRepository.findById(torneoId);

        if (!torneoOpt.isPresent()) {
            ApiResponse<List<Partido>> response = ApiResponseBuilder
                    .<List<Partido>>error("Torneo no encontrado", HttpStatus.NOT_FOUND.value())
                    .path("/api/partidos/torneo/" + torneoId)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        List<Partido> partidos = partidoService.findByTorneo(torneoOpt.get());

        ApiResponse<List<Partido>> response = ApiResponseBuilder
                .success(partidos)
                .message("Partidos del torneo: " + partidos.size())
                .path("/api/partidos/torneo/" + torneoId)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Crea un nuevo partido
     * Esto podría ser usado para programar partidos sin resultados inicialmente
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Partido>> crearPartido(@RequestBody Partido partido) {
        // Inicializar goles si no se especifican
        if (partido.getGolesLocal() == null) {
            partido.setGolesLocal(0);
        }
        if (partido.getGolesVisitante() == null) {
            partido.setGolesVisitante(0);
        }

        Partido partidoGuardado = partidoService.save(partido);

        ApiResponse<Partido> response = ApiResponseBuilder
                .created(partidoGuardado)
                .message("Partido creado exitosamente - Se publicó evento de actualización de estadísticas")
                .path("/api/partidos")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualiza un partido existente (usado para registrar/actualizar resultados)
     * Cuando se actualiza el resultado, se publica automáticamente un evento
     * que notifica a los observadores de la tabla de posiciones
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Partido>> actualizarPartido(
            @PathVariable Integer id,
            @RequestBody Partido partidoActualizado) {

        Optional<Partido> partidoExistente = partidoService.findById(id);

        if (partidoExistente.isPresent()) {
            Partido partido = partidoExistente.get();

            // Actualizar solo los campos necesarios
            if (partidoActualizado.getGolesLocal() != null) {
                partido.setGolesLocal(partidoActualizado.getGolesLocal());
            }
            if (partidoActualizado.getGolesVisitante() != null) {
                partido.setGolesVisitante(partidoActualizado.getGolesVisitante());
            }
            if (partidoActualizado.getFechaPartido() != null) {
                partido.setFechaPartido(partidoActualizado.getFechaPartido());
            }

            Partido partidoGuardado = partidoService.save(partido);

            ApiResponse<Partido> response = ApiResponseBuilder
                    .success(partidoGuardado)
                    .message("Partido actualizado exitosamente - Evento de actualización de estadísticas publicado")
                    .path("/api/partidos/" + id)
                    .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Partido> response = ApiResponseBuilder
                    .<Partido>error("Partido no encontrado", HttpStatus.NOT_FOUND.value())
                    .path("/api/partidos/" + id)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Registra el resultado de un partido (endpoint específico para resultados)
     * 
     * @param id             ID del partido
     * @param golesLocal     goles del equipo local
     * @param golesVisitante goles del equipo visitante
     */
    @PutMapping("/{id}/resultado")
    public ResponseEntity<ApiResponse<Partido>> registrarResultado(
            @PathVariable Integer id,
            @RequestParam Integer golesLocal,
            @RequestParam Integer golesVisitante) {

        Optional<Partido> partidoOpt = partidoService.findById(id);

        if (partidoOpt.isPresent()) {
            Partido partido = partidoOpt.get();
            // Guard: solo permitir registrar resultado si el torneo está ACTIVO
            if (partido.getTorneo() == null || !"ACTIVO".equalsIgnoreCase(partido.getTorneo().getEstado())) {
                ApiResponse<Partido> response = ApiResponseBuilder
                        .<Partido>error("No se puede registrar resultado: el torneo no está en estado ACTIVO",
                                HttpStatus.CONFLICT.value())
                        .path("/api/partidos/" + id + "/resultado")
                        .build();
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            partido.setGolesLocal(golesLocal);
            partido.setGolesVisitante(golesVisitante);
            partido.setJugado(true);

            Partido partidoGuardado = partidoService.save(partido);

            ApiResponse<Partido> response = ApiResponseBuilder
                    .success(partidoGuardado)
                    .message("Resultado registrado exitosamente - Tabla de posiciones actualizada automáticamente")
                    .path("/api/partidos/" + id + "/resultado")
                    .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Partido> response = ApiResponseBuilder
                    .<Partido>error("Partido no encontrado", HttpStatus.NOT_FOUND.value())
                    .path("/api/partidos/" + id + "/resultado")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Elimina un partido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPartido(@PathVariable Integer id) {
        if (partidoService.existsById(id)) {
            partidoService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

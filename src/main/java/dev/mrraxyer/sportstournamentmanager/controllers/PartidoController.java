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

/** Controlador de Partidos. */
@RestController
@RequestMapping("/api/partidos")
public class PartidoController {

    @Autowired
    private PartidoService partidoService;

    @Autowired
    private TorneoRepository torneoRepository;

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

    @PostMapping
    public ResponseEntity<ApiResponse<Partido>> crearPartido(@RequestBody Partido partido) {
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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Partido>> actualizarPartido(
            @PathVariable Integer id,
            @RequestBody Partido partidoActualizado) {

        Optional<Partido> partidoExistente = partidoService.findById(id);

        if (partidoExistente.isPresent()) {
            Partido partido = partidoExistente.get();

            if (partidoActualizado.getGolesLocal() != null) {
                partido.setGolesLocal(partidoActualizado.getGolesLocal());
            }
            if (partidoActualizado.getGolesVisitante() != null) {
                partido.setGolesVisitante(partidoActualizado.getGolesVisitante());
            }
            if (partidoActualizado.getFechaPartido() != null) {
                partido.setFechaPartido(partidoActualizado.getFechaPartido());
            }
            if (partidoActualizado.getEquipoLocal() != null) {
                partido.setEquipoLocal(partidoActualizado.getEquipoLocal());
            }
            if (partidoActualizado.getEquipoVisitante() != null) {
                partido.setEquipoVisitante(partidoActualizado.getEquipoVisitante());
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

    @PutMapping("/{id}/resultado")
    public ResponseEntity<ApiResponse<Partido>> registrarResultado(
            @PathVariable Integer id,
            @RequestParam Integer golesLocal,
            @RequestParam Integer golesVisitante) {

        Optional<Partido> partidoOpt = partidoService.findById(id);

        if (partidoOpt.isPresent()) {
            Partido partido = partidoOpt.get();
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

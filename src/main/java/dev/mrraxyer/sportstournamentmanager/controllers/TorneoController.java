package dev.mrraxyer.sportstournamentmanager.controllers;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.dto.ApiResponseBuilder;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.services.impl.TorneoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.mrraxyer.sportstournamentmanager.services.MatchSchedulerService;
import dev.mrraxyer.sportstournamentmanager.services.impl.PartidoService;
import dev.mrraxyer.sportstournamentmanager.models.Partido;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de Torneos
 */
@RestController
@RequestMapping("/api/torneos")
public class TorneoController {

    @Autowired
    private TorneoService torneoService;

    @Autowired
    private MatchSchedulerService matchSchedulerService;

    @Autowired
    private PartidoService partidoService;

    /**
     * Obtiene un torneo por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Torneo>> obtenerTorneo(@PathVariable Integer id) {
        Optional<Torneo> torneo = torneoService.findById(id);

        if (torneo.isPresent()) {
            ApiResponse<Torneo> response = ApiResponseBuilder
                .success(torneo.get())
                .message("Torneo encontrado")
                .path("/api/torneos/" + id)
                .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Torneo> response = ApiResponseBuilder
                .<Torneo>error("Torneo no encontrado", HttpStatus.NOT_FOUND.value())
                .path("/api/torneos/" + id)
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Obtiene todos los torneos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Torneo>>> listarTorneos() {
        List<Torneo> torneos = torneoService.findAll();

        ApiResponse<List<Torneo>> response = ApiResponseBuilder
            .success(torneos)
            .message("Total de torneos: " + torneos.size())
            .path("/api/torneos")
            .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Crea un nuevo torneo
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Torneo>> crearTorneo(@RequestBody Torneo torneo) {
        Torneo torneoGuardado = torneoService.save(torneo);

        ApiResponse<Torneo> response = ApiResponseBuilder
            .created(torneoGuardado)
            .message("Torneo creado exitosamente")
            .path("/api/torneos")
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualiza un torneo existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Torneo>> actualizarTorneo(
            @PathVariable Integer id,
            @RequestBody Torneo torneoActualizado) {

        Optional<Torneo> torneoExistente = torneoService.findById(id);

        if (torneoExistente.isPresent()) {
            torneoActualizado.setTorneosId(id);
            Torneo torneoGuardado = torneoService.save(torneoActualizado);

            ApiResponse<Torneo> response = ApiResponseBuilder
                .success(torneoGuardado)
                .message("Torneo actualizado exitosamente")
                .path("/api/torneos/" + id)
                .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Torneo> response = ApiResponseBuilder
                .<Torneo>error("Torneo no encontrado", HttpStatus.NOT_FOUND.value())
                .path("/api/torneos/" + id)
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Elimina un torneo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTorneo(@PathVariable Integer id) {
        if (torneoService.existsById(id)) {
            torneoService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Busca torneos por nombre (método específico de TorneoService)
     */
    @GetMapping("/buscar/nombre")
    public ResponseEntity<ApiResponse<List<Torneo>>> buscarPorNombre(
            @RequestParam String nombre) {

        List<Torneo> torneos = torneoService.findByNombre(nombre);

        ApiResponse<List<Torneo>> response = ApiResponseBuilder
            .success(torneos)
            .message("Torneos encontrados: " + torneos.size())
            .path("/api/torneos/buscar/nombre")
            .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Busca torneos por tipo de formato
     */
    @GetMapping("/buscar/formato")
    public ResponseEntity<ApiResponse<List<Torneo>>> buscarPorFormato(
            @RequestParam String tipoFormato) {

        List<Torneo> torneos = torneoService.findByTipoFormato(tipoFormato);

        ApiResponse<List<Torneo>> response = ApiResponseBuilder
            .success(torneos)
            .message("Torneos encontrados: " + torneos.size())
            .path("/api/torneos/buscar/formato")
            .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Genera el calendario de partidos para un torneo según su formato configurado.
     * Retorna 409 Conflict si el torneo ya tiene partidos para evitar duplicados.
     *
     * @param id el ID del torneo
     * @return la lista de partidos generados, o una respuesta de error
     */
    @PostMapping("/{id}/generar-calendario")
    public ResponseEntity<ApiResponse<List<Partido>>> generarCalendario(@PathVariable Integer id) {
        Optional<Torneo> torneoOpt = torneoService.findById(id);

        if (!torneoOpt.isPresent()) {
            ApiResponse<List<Partido>> response = ApiResponseBuilder
                .<List<Partido>>error("Torneo no encontrado", HttpStatus.NOT_FOUND.value())
                .path("/api/torneos/" + id + "/generar-calendario")
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Torneo torneo = torneoOpt.get();
        List<Partido> existing = partidoService.findByTorneo(torneo);

        if (!existing.isEmpty()) {
            ApiResponse<List<Partido>> response = ApiResponseBuilder
                .<List<Partido>>error("Este torneo ya tiene partidos generados", HttpStatus.CONFLICT.value())
                .path("/api/torneos/" + id + "/generar-calendario")
                .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        List<Partido> partidos = matchSchedulerService.scheduleMatchesUsingTorneoFormat(torneo, LocalDate.now());

        ApiResponse<List<Partido>> response = ApiResponseBuilder
            .created(partidos)
            .message("Calendario generado exitosamente: " + partidos.size() + " partidos")
            .path("/api/torneos/" + id + "/generar-calendario")
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}




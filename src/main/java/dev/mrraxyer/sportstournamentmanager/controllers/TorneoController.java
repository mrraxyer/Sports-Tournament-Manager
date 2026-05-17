package dev.mrraxyer.sportstournamentmanager.controllers;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.dto.ApiResponseBuilder;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.services.impl.TorneoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de Torneos - Ejemplo de uso de Genéricos
 *
 * Demuestra reutilización del patrón genérico con una entidad diferente (Torneo).
 * La estructura es idéntica a UsuarioController, solo cambia el tipo genérico
 * y los métodos específicos de Torneo.
 *
 * Esto muestra el poder de los genéricos: mismo patrón, múltiples entidades.
 */
@RestController
@RequestMapping("/api/torneos")
public class TorneoController {

    @Autowired
    private TorneoService torneoService;

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
}




package dev.mrraxyer.sportstournamentmanager.controllers;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.dto.ApiResponseBuilder;
import dev.mrraxyer.sportstournamentmanager.models.Equipo;
import dev.mrraxyer.sportstournamentmanager.services.impl.EquipoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de Equipos
 */
@RestController
@RequestMapping("/api/equipos")
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    /**
     * Obtiene un equipo por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Equipo>> obtenerEquipo(@PathVariable Integer id) {
        Optional<Equipo> equipo = equipoService.findById(id);

        if (equipo.isPresent()) {
            ApiResponse<Equipo> response = ApiResponseBuilder
                .success(equipo.get())
                .message("Equipo encontrado")
                .path("/api/equipos/" + id)
                .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Equipo> response = ApiResponseBuilder
                .<Equipo>error("Equipo no encontrado", HttpStatus.NOT_FOUND.value())
                .path("/api/equipos/" + id)
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Obtiene todos los equipos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Equipo>>> listarEquipos() {
        List<Equipo> equipos = equipoService.findAll();

        ApiResponse<List<Equipo>> response = ApiResponseBuilder
            .success(equipos)
            .message("Total de equipos: " + equipos.size())
            .path("/api/equipos")
            .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Crea un nuevo equipo
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Equipo>> crearEquipo(@RequestBody Equipo equipo) {
        Equipo equipoGuardado = equipoService.save(equipo);

        ApiResponse<Equipo> response = ApiResponseBuilder
            .created(equipoGuardado)
            .message("Equipo creado exitosamente")
            .path("/api/equipos")
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualiza un equipo existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Equipo>> actualizarEquipo(
            @PathVariable Integer id,
            @RequestBody Equipo equipoActualizado) {

        Optional<Equipo> equipoExistente = equipoService.findById(id);

        if (equipoExistente.isPresent()) {
            Equipo existing = equipoExistente.get();
            existing.setNombre(equipoActualizado.getNombre());
            if (equipoActualizado.getTorneo() != null) existing.setTorneo(equipoActualizado.getTorneo());
            if (equipoActualizado.getCapitan() != null) existing.setCapitan(equipoActualizado.getCapitan());
            Equipo equipoGuardado = equipoService.save(existing);

            ApiResponse<Equipo> response = ApiResponseBuilder
                .success(equipoGuardado)
                .message("Equipo actualizado exitosamente")
                .path("/api/equipos/" + id)
                .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Equipo> response = ApiResponseBuilder
                .<Equipo>error("Equipo no encontrado", HttpStatus.NOT_FOUND.value())
                .path("/api/equipos/" + id)
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Elimina un equipo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEquipo(@PathVariable Integer id) {
        if (equipoService.existsById(id)) {
            equipoService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

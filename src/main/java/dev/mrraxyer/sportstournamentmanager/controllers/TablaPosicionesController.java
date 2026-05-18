package dev.mrraxyer.sportstournamentmanager.controllers;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.dto.ApiResponseBuilder;
import dev.mrraxyer.sportstournamentmanager.models.TablaPosiciones;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.repositories.TorneoRepository;
import dev.mrraxyer.sportstournamentmanager.services.impl.TablaPosicionesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de Tabla de Posiciones
 * Permite consultar las estadísticas actualizadas reactivamente
 * cuando se registran resultados de partidos
 */
@RestController
@RequestMapping("/api/tabla-posiciones")
public class TablaPosicionesController {

    @Autowired
    private TablaPosicionesService tablaPosicionesService;

    @Autowired
    private TorneoRepository torneoRepository;

    /**
     * Obtiene la tabla de posiciones de un torneo específico
     * Ordenada por puntos (descendente) y diferencia de goles
     *
     * @param torneoId ID del torneo
     */
    @GetMapping("/torneo/{torneoId}")
    public ResponseEntity<ApiResponse<List<TablaPosiciones>>> obtenerTablaPorTorneo(
            @PathVariable Integer torneoId) {

        Optional<Torneo> torneoOpt = torneoRepository.findById(torneoId);

        if (!torneoOpt.isPresent()) {
            ApiResponse<List<TablaPosiciones>> response = ApiResponseBuilder
                .<List<TablaPosiciones>>error("Torneo no encontrado", HttpStatus.NOT_FOUND.value())
                .path("/api/tabla-posiciones/torneo/" + torneoId)
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Torneo torneo = torneoOpt.get();
        List<TablaPosiciones> posiciones = tablaPosicionesService.findAll().stream()
            .filter(tp -> tp.getTorneo().getTorneosId().equals(torneoId))
            .sorted((a, b) -> {
                // Ordenar por puntos descendente
                int puntosComparison = Integer.compare(b.getPuntos(), a.getPuntos());
                if (puntosComparison != 0) {
                    return puntosComparison;
                }
                // Si puntos iguales, ordenar por diferencia de goles descendente
                int difA = a.getGolesAFavor() - a.getGolesEnContra();
                int difB = b.getGolesAFavor() - b.getGolesEnContra();
                return Integer.compare(difB, difA);
            })
            .toList();

        ApiResponse<List<TablaPosiciones>> response = ApiResponseBuilder
            .success(posiciones)
            .message("Tabla de posiciones del torneo " + torneo.getNombre() + ": " + posiciones.size() + " equipos")
            .path("/api/tabla-posiciones/torneo/" + torneoId)
            .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene la posición de un equipo específico en un torneo
     *
     * @param torneoId ID del torneo
     * @param equipoId ID del equipo
     */
    @GetMapping("/torneo/{torneoId}/equipo/{equipoId}")
    public ResponseEntity<ApiResponse<TablaPosiciones>> obtenerPosicionEquipo(
            @PathVariable Integer torneoId,
            @PathVariable Integer equipoId) {

        Optional<TablaPosiciones> posicionOpt = tablaPosicionesService.findAll().stream()
            .filter(tp -> tp.getTorneo().getTorneosId().equals(torneoId) &&
                         tp.getEquipo().getEquiposId().equals(equipoId))
            .findFirst();

        if (posicionOpt.isPresent()) {
            ApiResponse<TablaPosiciones> response = ApiResponseBuilder
                .success(posicionOpt.get())
                .message("Posición encontrada")
                .path("/api/tabla-posiciones/torneo/" + torneoId + "/equipo/" + equipoId)
                .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<TablaPosiciones> response = ApiResponseBuilder
                .<TablaPosiciones>error("Posición no encontrada", HttpStatus.NOT_FOUND.value())
                .path("/api/tabla-posiciones/torneo/" + torneoId + "/equipo/" + equipoId)
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Obtiene todas las posiciones
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TablaPosiciones>>> listarTodasLasPosiciones() {
        List<TablaPosiciones> posiciones = tablaPosicionesService.findAll();

        ApiResponse<List<TablaPosiciones>> response = ApiResponseBuilder
            .success(posiciones)
            .message("Total de posiciones: " + posiciones.size())
            .path("/api/tabla-posiciones")
            .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene una posición específica por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TablaPosiciones>> obtenerPosicion(@PathVariable Integer id) {
        Optional<TablaPosiciones> posicion = tablaPosicionesService.findById(id);

        if (posicion.isPresent()) {
            ApiResponse<TablaPosiciones> response = ApiResponseBuilder
                .success(posicion.get())
                .message("Posición encontrada")
                .path("/api/tabla-posiciones/" + id)
                .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<TablaPosiciones> response = ApiResponseBuilder
                .<TablaPosiciones>error("Posición no encontrada", HttpStatus.NOT_FOUND.value())
                .path("/api/tabla-posiciones/" + id)
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}


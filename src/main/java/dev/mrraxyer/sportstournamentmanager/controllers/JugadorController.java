package dev.mrraxyer.sportstournamentmanager.controllers;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.dto.ApiResponseBuilder;
import dev.mrraxyer.sportstournamentmanager.models.Jugador;
import dev.mrraxyer.sportstournamentmanager.services.impl.JugadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/** Controlador de Jugadores. */
@RestController
@RequestMapping("/api/jugadores")
public class JugadorController {

    @Autowired
    private JugadorService jugadorService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Jugador>> obtenerJugador(@PathVariable Integer id) {
        Optional<Jugador> jugador = jugadorService.findById(id);

        if (jugador.isPresent()) {
            ApiResponse<Jugador> response = ApiResponseBuilder
                .success(jugador.get())
                .message("Jugador encontrado")
                .path("/api/jugadores/" + id)
                .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Jugador> response = ApiResponseBuilder
                .<Jugador>error("Jugador no encontrado", HttpStatus.NOT_FOUND.value())
                .path("/api/jugadores/" + id)
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Jugador>>> listarJugadores() {
        List<Jugador> jugadores = jugadorService.findAll();

        ApiResponse<List<Jugador>> response = ApiResponseBuilder
            .success(jugadores)
            .message("Total de jugadores: " + jugadores.size())
            .path("/api/jugadores")
            .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Jugador>> crearJugador(@RequestBody Jugador jugador) {
        Jugador jugadorGuardado = jugadorService.save(jugador);

        ApiResponse<Jugador> response = ApiResponseBuilder
            .created(jugadorGuardado)
            .message("Jugador creado exitosamente")
            .path("/api/jugadores")
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Jugador>> actualizarJugador(
            @PathVariable Integer id,
            @RequestBody Jugador jugadorActualizado) {

        Optional<Jugador> jugadorExistente = jugadorService.findById(id);

        if (jugadorExistente.isPresent()) {
            Jugador existing = jugadorExistente.get();
            existing.setNombre(jugadorActualizado.getNombre());
            existing.setNumeroCamiseta(jugadorActualizado.getNumeroCamiseta());
            Jugador jugadorGuardado = jugadorService.save(existing);

            ApiResponse<Jugador> response = ApiResponseBuilder
                .success(jugadorGuardado)
                .message("Jugador actualizado exitosamente")
                .path("/api/jugadores/" + id)
                .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Jugador> response = ApiResponseBuilder
                .<Jugador>error("Jugador no encontrado", HttpStatus.NOT_FOUND.value())
                .path("/api/jugadores/" + id)
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarJugador(@PathVariable Integer id) {
        if (jugadorService.existsById(id)) {
            jugadorService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

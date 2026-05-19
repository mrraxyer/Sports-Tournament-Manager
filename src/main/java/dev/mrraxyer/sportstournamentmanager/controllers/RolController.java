package dev.mrraxyer.sportstournamentmanager.controllers;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.dto.ApiResponseBuilder;
import dev.mrraxyer.sportstournamentmanager.models.Rol;
import dev.mrraxyer.sportstournamentmanager.repositories.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/** Controlador de Roles. */
@RestController
@RequestMapping("/api/roles")
public class RolController {

    @Autowired
    private RolRepository rolRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Rol>>> listarRoles() {
        List<Rol> roles = rolRepository.findAll();

        ApiResponse<List<Rol>> response = ApiResponseBuilder
            .success(roles)
            .message("Total de roles: " + roles.size())
            .path("/api/roles")
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Rol>> obtenerRol(@PathVariable Integer id) {
        Optional<Rol> rol = rolRepository.findById(id);

        if (rol.isPresent()) {
            ApiResponse<Rol> response = ApiResponseBuilder
                .success(rol.get())
                .message("Rol encontrado")
                .path("/api/roles/" + id)
                .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Rol> response = ApiResponseBuilder
                .<Rol>error("Rol no encontrado", 404)
                .path("/api/roles/" + id)
                .build();
            return ResponseEntity.status(404).body(response);
        }
    }
}

package dev.mrraxyer.sportstournamentmanager.controllers;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.dto.ApiResponseBuilder;
import dev.mrraxyer.sportstournamentmanager.dto.LoginRequest;
import dev.mrraxyer.sportstournamentmanager.models.Usuario;
import dev.mrraxyer.sportstournamentmanager.services.impl.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getContrasena())
            );

            // Obtener usuario para devolver datos (sin contraseña)
            Optional<Usuario> usuarioOpt = usuarioService.findByCorreo(request.getCorreo());
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                usuario.setContrasena(null);
                ApiResponse<Object> response = ApiResponseBuilder
                        .<Object>success(usuario)
                        .message("Autenticación exitosa")
                        .path("/api/auth/login")
                        .build();
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<Object> response = ApiResponseBuilder
                        .<Object>error("Usuario no encontrado después de la autenticación", HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .path("/api/auth/login")
                        .build();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (BadCredentialsException ex) {
            ApiResponse<Object> response = ApiResponseBuilder
                    .<Object>error("Credenciales inválidas", HttpStatus.UNAUTHORIZED.value())
                    .path("/api/auth/login")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}


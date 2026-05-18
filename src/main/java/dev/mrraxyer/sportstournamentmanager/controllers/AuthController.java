package dev.mrraxyer.sportstournamentmanager.controllers;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.dto.ApiResponseBuilder;
import dev.mrraxyer.sportstournamentmanager.dto.LoginResponse;
import dev.mrraxyer.sportstournamentmanager.dto.LoginRequest;
import dev.mrraxyer.sportstournamentmanager.security.AuthTokenService;
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

    @Autowired
    private AuthTokenService authTokenService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getContrasena())
            );

            Optional<dev.mrraxyer.sportstournamentmanager.models.Usuario> usuarioOpt = usuarioService.findByCorreo(request.getCorreo());
            if (usuarioOpt.isPresent()) {
                dev.mrraxyer.sportstournamentmanager.models.Usuario usuario = usuarioOpt.get();
                AuthTokenService.LoginToken loginToken = authTokenService.createToken(usuario, auth.getAuthorities());
                LoginResponse responseBody = new LoginResponse(
                        loginToken.accessToken(),
                        "Bearer",
                        loginToken.expiresAt(),
                        loginToken.usuario()
                );

                ApiResponse<LoginResponse> response = ApiResponseBuilder
                        .success(responseBody)
                        .message("Autenticación exitosa")
                        .path("/api/auth/login")
                        .build();
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<LoginResponse> response = ApiResponseBuilder
                        .<LoginResponse>error("Usuario no encontrado después de la autenticación", HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .path("/api/auth/login")
                        .build();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (BadCredentialsException ex) {
            ApiResponse<LoginResponse> response = ApiResponseBuilder
                    .<LoginResponse>error("Credenciales inválidas", HttpStatus.UNAUTHORIZED.value())
                    .path("/api/auth/login")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}


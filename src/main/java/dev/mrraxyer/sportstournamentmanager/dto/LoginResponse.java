package dev.mrraxyer.sportstournamentmanager.dto;

import java.time.Instant;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        UsuarioSesionDto usuario
) {}

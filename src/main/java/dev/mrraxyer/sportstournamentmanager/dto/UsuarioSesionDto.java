package dev.mrraxyer.sportstournamentmanager.dto;

public record UsuarioSesionDto(
        Integer usuariosId,
        String nombre,
        String correo,
        String rol
) {}

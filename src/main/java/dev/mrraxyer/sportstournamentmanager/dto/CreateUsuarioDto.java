package dev.mrraxyer.sportstournamentmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear usuarios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUsuarioDto {
    private String nombre;
    private String correo;
    private String contrasena;
    private Integer rolId;
}

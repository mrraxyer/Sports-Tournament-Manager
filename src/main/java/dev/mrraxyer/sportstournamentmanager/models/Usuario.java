package dev.mrraxyer.sportstournamentmanager.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Usuario: Almacena la información de autenticación y perfil de los usuarios del sistema.
 * Contiene datos personales, credenciales y asignación de roles.
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer usuariosId;
    
    @ManyToOne
    @JoinColumn(name = "roles_id")
    private Rol rol;
    
    @Column(nullable = false, length = 255)
    private String nombre;
    
    @Column(nullable = false, unique = true, length = 255)
    private String correo;
    
    @Column(nullable = false, length = 255)
    private String contrasena;
}


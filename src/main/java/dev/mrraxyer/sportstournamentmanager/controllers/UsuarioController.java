package dev.mrraxyer.sportstournamentmanager.controllers;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.dto.ApiResponseBuilder;
import dev.mrraxyer.sportstournamentmanager.models.Usuario;
import dev.mrraxyer.sportstournamentmanager.services.impl.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de Usuarios - Ejemplo de uso de Genéricos
 *
 * Demuestra:
 * 1. Uso de ApiResponse<T> genérica para respuestas consistentes
 * 2. Uso de ApiResponseBuilder<T> para construcción fluida
 * 3. Servicio genérico BaseService<Usuario, Integer>
 * 4. Repositorio genérico BaseRepository<Usuario, Integer>
 *
 * La arquitectura es completamente reusable. Para crear un controlador similar
 * de Torneos, Equipos, etc., solo necesitarías cambiar:
 * - El tipo genérico (Usuario -> Torneo, Equipo, etc.)
 * - La clase de servicio (UsuarioService -> TorneoService, etc.)
 * - Los DTOs específicos si los hay
 *
 * TODO: Implementar DTOs de entrada (DTO) para validación de datos
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Obtiene un usuario por ID
     * Ejemplo de ApiResponse<T> con tipo específico Usuario
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Usuario>> obtenerUsuario(@PathVariable Integer id) {
        Optional<Usuario> usuario = usuarioService.findById(id);

        if (usuario.isPresent()) {
            // Respuesta exitosa con datos
            ApiResponse<Usuario> response = ApiResponseBuilder
                .success(usuario.get())
                .message("Usuario encontrado")
                .path("/api/usuarios/" + id)
                .build();
            return ResponseEntity.ok(response);
        } else {
            // Respuesta de error
            ApiResponse<Usuario> response = ApiResponseBuilder
                .<Usuario>error("Usuario no encontrado", HttpStatus.NOT_FOUND.value())
                .path("/api/usuarios/" + id)
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Obtiene todos los usuarios
     * Ejemplo de ApiResponse<T> con List<Usuario>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Usuario>>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.findAll();

        ApiResponse<List<Usuario>> response = ApiResponseBuilder
            .success(usuarios)
            .message("Total de usuarios: " + usuarios.size())
            .path("/api/usuarios")
            .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Crea un nuevo usuario
     * Ejemplo de ApiResponse<T> para recurso creado (201)
     *
     * TODO: Usar @Valid y DTO para validar entrada
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Usuario>> crearUsuario(@RequestBody Usuario usuario) {
        Usuario usuarioGuardado = usuarioService.save(usuario);

        ApiResponse<Usuario> response = ApiResponseBuilder
            .created(usuarioGuardado)
            .message("Usuario creado exitosamente")
            .path("/api/usuarios")
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualiza un usuario existente
     * Ejemplo de actualización con BaseService genérico
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Usuario>> actualizarUsuario(
            @PathVariable Integer id,
            @RequestBody Usuario usuarioActualizado) {

        Optional<Usuario> usuarioExistente = usuarioService.findById(id);

        if (usuarioExistente.isPresent()) {
            usuarioActualizado.setUsuariosId(id);
            Usuario usuarioGuardado = usuarioService.save(usuarioActualizado);

            ApiResponse<Usuario> response = ApiResponseBuilder
                .success(usuarioGuardado)
                .message("Usuario actualizado exitosamente")
                .path("/api/usuarios/" + id)
                .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Usuario> response = ApiResponseBuilder
                .<Usuario>error("Usuario no encontrado", HttpStatus.NOT_FOUND.value())
                .path("/api/usuarios/" + id)
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Elimina un usuario
     * Ejemplo de operación sin contenido (204)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id) {
        if (usuarioService.existsById(id)) {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Busca usuarios por correo
     * Ejemplo de método específico de UsuarioService
     */
    @GetMapping("/buscar/correo")
    public ResponseEntity<ApiResponse<Usuario>> buscarPorCorreo(
            @RequestParam String correo) {
        
        var usuario = usuarioService.findByCorreo(correo);
        
        if (usuario.isPresent()) {
            ApiResponse<Usuario> response = ApiResponseBuilder
                .success(usuario.get())
                .message("Usuario encontrado por correo")
                .path("/api/usuarios/buscar/correo")
                .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Usuario> response = ApiResponseBuilder
                .<Usuario>error("Usuario no encontrado", HttpStatus.NOT_FOUND.value())
                .path("/api/usuarios/buscar/correo")
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}






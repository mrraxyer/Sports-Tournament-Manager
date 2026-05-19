package dev.mrraxyer.sportstournamentmanager.controllers;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.dto.ApiResponseBuilder;
import dev.mrraxyer.sportstournamentmanager.dto.CreateUsuarioDto;
import dev.mrraxyer.sportstournamentmanager.models.Usuario;
import dev.mrraxyer.sportstournamentmanager.models.Rol;
import dev.mrraxyer.sportstournamentmanager.services.impl.UsuarioService;
import dev.mrraxyer.sportstournamentmanager.repositories.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de Usuarios
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolRepository rolRepository;

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
     * Crea o actualiza un usuario con rol (UPSERT)
     */
    @PostMapping("/crear")
    public ResponseEntity<ApiResponse<Usuario>> crearUsuarioConRol(@RequestBody CreateUsuarioDto dto) {
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            ApiResponse<Usuario> response = ApiResponseBuilder
                .<Usuario>error("Nombre es requerido", HttpStatus.BAD_REQUEST.value())
                .path("/api/usuarios/crear")
                .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (dto.getCorreo() == null || dto.getCorreo().trim().isEmpty()) {
            ApiResponse<Usuario> response = ApiResponseBuilder
                .<Usuario>error("Correo es requerido", HttpStatus.BAD_REQUEST.value())
                .path("/api/usuarios/crear")
                .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Rol rol = null;
        if (dto.getRolId() != null) {
            Optional<Rol> rolOpt = rolRepository.findById(dto.getRolId());
            if (rolOpt.isPresent()) {
                rol = rolOpt.get();
            }
        }

        Optional<Usuario> existente = usuarioService.findByCorreo(dto.getCorreo());
        Usuario usuario;
        boolean isCreated = false;

        if (existente.isPresent()) {
            usuario = existente.get();
            usuario.setNombre(dto.getNombre());
            if (dto.getContrasena() != null && !dto.getContrasena().trim().isEmpty()) {
                usuario.setContrasena(dto.getContrasena());
            }
            if (rol != null) {
                usuario.setRol(rol);
            }
        } else {
            usuario = new Usuario();
            usuario.setNombre(dto.getNombre());
            usuario.setCorreo(dto.getCorreo());
            usuario.setContrasena(dto.getContrasena());
            usuario.setRol(rol);
            isCreated = true;
        }

        Usuario usuarioGuardado = usuarioService.save(usuario);

        ApiResponse<Usuario> response = ApiResponseBuilder
            .created(usuarioGuardado)
            .message(isCreated ? "Usuario creado exitosamente" : "Usuario actualizado exitosamente")
            .path("/api/usuarios/crear")
            .build();
        return ResponseEntity.status(isCreated ? HttpStatus.CREATED : HttpStatus.OK).body(response);
    }

    /**
     * Crea un nuevo usuario (legacy endpoint)
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
     * Obtiene usuarios por rol
     */
    @GetMapping("/por-rol")
    public ResponseEntity<ApiResponse<List<Usuario>>> usuariosPorRol(@RequestParam String rolNombre) {
        Optional<Rol> rol = rolRepository.findByNombre(rolNombre);

        if (rol.isEmpty()) {
            ApiResponse<List<Usuario>> response = ApiResponseBuilder
                .<List<Usuario>>error("Rol no encontrado", HttpStatus.NOT_FOUND.value())
                .path("/api/usuarios/por-rol")
                .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        List<Usuario> usuarios = usuarioService.findAll().stream()
            .filter(u -> u.getRol() != null && u.getRol().getRolesId().equals(rol.get().getRolesId()))
            .toList();

        ApiResponse<List<Usuario>> response = ApiResponseBuilder
            .success(usuarios)
            .message("Usuarios encontrados: " + usuarios.size())
            .path("/api/usuarios/por-rol")
            .build();
        return ResponseEntity.ok(response);
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






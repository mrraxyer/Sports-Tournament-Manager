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

/** Controlador de Usuarios. */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolRepository rolRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Usuario>> obtenerUsuario(@PathVariable Integer id) {
        Optional<Usuario> usuario = usuarioService.findById(id);

        if (usuario.isPresent()) {
            ApiResponse<Usuario> response = ApiResponseBuilder
                .success(usuario.get())
                .message("Usuario encontrado")
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

        if (existente.isPresent()) {
            ApiResponse<Usuario> response = ApiResponseBuilder
                .<Usuario>error("El correo ya está asociado a otro usuario", HttpStatus.BAD_REQUEST.value())
                .path("/api/usuarios/crear")
                .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setCorreo(dto.getCorreo());
        usuario.setContrasena(dto.getContrasena());
        usuario.setRol(rol);
        
        Usuario usuarioGuardado = usuarioService.save(usuario);

        ApiResponse<Usuario> response = ApiResponseBuilder
            .created(usuarioGuardado)
            .message("Usuario creado exitosamente")
            .path("/api/usuarios/crear")
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Usuario>> crearUsuario(@RequestBody Usuario usuario) {
        if (usuarioService.findByCorreo(usuario.getCorreo()).isPresent()) {
            ApiResponse<Usuario> response = ApiResponseBuilder
                .<Usuario>error("El correo ya está asociado a otro usuario", HttpStatus.BAD_REQUEST.value())
                .path("/api/usuarios")
                .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Usuario usuarioGuardado = usuarioService.save(usuario);

        ApiResponse<Usuario> response = ApiResponseBuilder
            .created(usuarioGuardado)
            .message("Usuario creado exitosamente")
            .path("/api/usuarios")
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Usuario>> actualizarUsuario(
            @PathVariable Integer id,
            @RequestBody CreateUsuarioDto dto) {

        Optional<Usuario> usuarioExistenteOpt = usuarioService.findById(id);

        if (usuarioExistenteOpt.isPresent()) {
            Usuario usuarioExistente = usuarioExistenteOpt.get();

            // Validar que el nuevo correo no esté en uso por OTRO usuario
            if (!usuarioExistente.getCorreo().equals(dto.getCorreo())) {
                Optional<Usuario> otroUsuario = usuarioService.findByCorreo(dto.getCorreo());
                if (otroUsuario.isPresent()) {
                    ApiResponse<Usuario> response = ApiResponseBuilder
                        .<Usuario>error("El correo ya está asociado a otro usuario", HttpStatus.BAD_REQUEST.value())
                        .path("/api/usuarios/" + id)
                        .build();
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }

            usuarioExistente.setNombre(dto.getNombre());
            usuarioExistente.setCorreo(dto.getCorreo());
            
            if (dto.getContrasena() != null && !dto.getContrasena().trim().isEmpty()) {
                usuarioExistente.setContrasena(dto.getContrasena());
            }

            if (dto.getRolId() != null) {
                rolRepository.findById(dto.getRolId()).ifPresent(usuarioExistente::setRol);
            }

            Usuario usuarioGuardado = usuarioService.save(usuarioExistente);

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id) {
        if (usuarioService.existsById(id)) {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

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






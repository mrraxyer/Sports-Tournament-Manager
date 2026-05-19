package dev.mrraxyer.sportstournamentmanager.services.impl;

import dev.mrraxyer.sportstournamentmanager.models.Usuario;
import dev.mrraxyer.sportstournamentmanager.repositories.BaseRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.UsuarioRepository;
import dev.mrraxyer.sportstournamentmanager.services.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Servicio de Usuario. */
@Service
public class UsuarioService extends BaseService<Usuario, Integer> {
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    protected BaseRepository<Usuario, Integer> getRepository() {
        return usuarioRepository;
    }
    
    public Optional<Usuario> findByCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    /** No recodifica si la contraseña ya es SHA-1 (hex de 40 caracteres). */
    @Override
    public Usuario save(Usuario usuario) {
        if (usuario.getContrasena() != null) {
            String raw = usuario.getContrasena();
            if (!isProbablySha1(raw)) {
                usuario.setContrasena(passwordEncoder.encode(raw));
            }
        }
        return super.save(usuario);
    }

    private boolean isProbablySha1(String s) {
        if (s == null) return false;
        if (s.length() != 40) return false;
        return s.matches("^[0-9a-fA-F]{40}$");
    }
}


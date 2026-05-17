package dev.mrraxyer.sportstournamentmanager.services.impl;

import dev.mrraxyer.sportstournamentmanager.models.Usuario;
import dev.mrraxyer.sportstournamentmanager.repositories.BaseRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.UsuarioRepository;
import dev.mrraxyer.sportstournamentmanager.services.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio de Usuario
 *
 * Extiende BaseService<Usuario, Integer> para herdar todas las operaciones CRUD genéricas.
 *
 */
@Service
public class UsuarioService extends BaseService<Usuario, Integer> {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Override
    protected BaseRepository<Usuario, Integer> getRepository() {
        return usuarioRepository;
    }
    
    /**
     * Busca un usuario por correo electrónico
     * Método específico que extiende la funcionalidad base
     */
    public Optional<Usuario> findByCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }
}


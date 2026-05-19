package dev.mrraxyer.sportstournamentmanager.services.impl;

import dev.mrraxyer.sportstournamentmanager.models.Rol;
import dev.mrraxyer.sportstournamentmanager.models.Usuario;
import dev.mrraxyer.sportstournamentmanager.repositories.RolRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.UsuarioRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Crea roles y usuario maestro al iniciar si no existen. */
@Service
public class InitializationService {

    private static final Logger logger = LoggerFactory.getLogger(InitializationService.class);
    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Value("${app.init.master-username:admin}")
    private String masterUsername;

    @Value("${app.init.master-email:admin@sportstournament.com}")
    private String masterEmail;

    @Value("${app.init.master-password:admin123}")
    private String masterPassword;

    @Value("${app.init.enabled:true}")
    private boolean initEnabled;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaultData() {
        if (!initEnabled) {
            logger.info("Inicialización deshabilitada");
            return;
        }

        logger.info("Iniciando proceso de inicialización de datos...");
        createDefaultRoles();
        createMasterUser();
        logger.info("Proceso de inicialización completado");
    }

    /** Crea los roles predeterminados si no existen. */
    private void createDefaultRoles() {
        String[] defaultRoles = {"ADMIN", "USER", "TEAM_CAPTAIN"};

        for (String roleName : defaultRoles) {
            Optional<Rol> existingRol = rolRepository.findByNombre(roleName);

            if (existingRol.isEmpty()) {
                Rol newRol = new Rol();
                newRol.setNombre(roleName);
                rolRepository.save(newRol);
                logger.info("Rol creado: {}", roleName);
            } else {
                logger.debug("Rol ya existe: {}", roleName);
            }
        }
    }

    /** Crea el usuario maestro si no existe. */
    private void createMasterUser() {
        Optional<Usuario> existingUser = usuarioRepository.findByCorreo(masterEmail);
        if (existingUser.isPresent()) {
            migrateLegacyMasterPassword(existingUser.get());
            return;
        }

        Optional<Rol> adminRol = rolRepository.findByNombre("ADMIN");

        if (adminRol.isEmpty()) {
            logger.error("No se puede crear usuario maestro: rol ADMIN no existe");
            return;
        }

        Usuario masterUser = new Usuario();
        masterUser.setNombre(masterUsername);
        masterUser.setCorreo(masterEmail);
        // UsuarioService aplica PasswordEncoder para almacenar el hash.
        masterUser.setContrasena(masterPassword);
        masterUser.setRol(adminRol.get());

        usuarioService.save(masterUser);
        logger.info("Usuario maestro creado: {} ({})", masterEmail, masterUsername);
    }

    /** Migra contraseña del usuario maestro si no está hasheada (instalaciones previas). */
    private void migrateLegacyMasterPassword(Usuario existingMasterUser) {
        String currentPassword = existingMasterUser.getContrasena();
        if (isProbablySha1(currentPassword)) {
            logger.debug("Usuario maestro ya existe y su contraseña está hasheada: {}", masterEmail);
            return;
        }

        existingMasterUser.setContrasena(masterPassword);
        usuarioService.save(existingMasterUser);
        logger.warn("Usuario maestro existente migrado a contraseña hasheada desde variable de entorno: {}", masterEmail);
    }

    private boolean isProbablySha1(String value) {
        if (value == null || value.length() != 40) {
            return false;
        }
        return value.matches("^[0-9a-fA-F]{40}$");
    }
}





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

/**
 * Servicio de Inicialización
 *
 * Se ejecuta automáticamente al iniciar la aplicación.
 * Crea los roles predeterminados y el usuario maestro si no existen.
 * Las credenciales del usuario maestro se leen desde variables de entorno.
 */
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

    /**
     * Se ejecuta cuando la aplicación ha terminado de iniciarse.
     * Crea los roles y el usuario maestro si no existen.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaultData() {
        if (!initEnabled) {
            logger.info("Inicialización deshabilitada");
            return;
        }

        logger.info("Iniciando proceso de inicialización de datos...");

        // Crear roles por defecto
        createDefaultRoles();

        // Crear usuario maestro
        createMasterUser();

        logger.info("Proceso de inicialización completado");
    }

    /**
     * Crea los roles predeterminados si no existen.
     */
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

    /**
     * Crea el usuario maestro si no existe.
     * Lee las credenciales desde las variables de entorno/propiedades.
     */
    private void createMasterUser() {
        Optional<Usuario> existingUser = usuarioRepository.findByCorreo(masterEmail);
        if (existingUser.isPresent()) {
            migrateLegacyMasterPassword(existingUser.get());
            return;
        }

        // Obtener el rol ADMIN
        Optional<Rol> adminRol = rolRepository.findByNombre("ADMIN");

        if (adminRol.isEmpty()) {
            logger.error("No se puede crear usuario maestro: rol ADMIN no existe");
            return;
        }

        // Crear el usuario maestro
        Usuario masterUser = new Usuario();
        masterUser.setNombre(masterUsername);
        masterUser.setCorreo(masterEmail);
        // UsuarioService aplica PasswordEncoder para almacenar el hash.
        masterUser.setContrasena(masterPassword);
        masterUser.setRol(adminRol.get());

        usuarioService.save(masterUser);
        logger.info("Usuario maestro creado: {} ({})", masterEmail, masterUsername);
    }

    /**
     * Repara instalaciones previas donde el usuario maestro pudo guardarse sin hash.
     * Solo migra si la contraseña actual no parece SHA-1.
     */
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





package dev.mrraxyer.sportstournamentmanager.config;

import dev.mrraxyer.sportstournamentmanager.repositories.GlobalPropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Componente Spring que sincroniza el singleton GlobalConfig con la base de datos
 * al iniciarse la aplicación.
 */
@Component
public class GlobalConfigLoader {

    @Autowired(required = false)
    private GlobalPropertyRepository repo;

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed() {
        // Carga overrides desde la BD si el repositorio está presente
        GlobalConfig cfg = GlobalConfig.getInstance();
        if (repo != null) {
            try {
                cfg.loadOverridesFromRepository(repo);
            } catch (Exception e) {
                System.err.println("Error cargando overrides de GlobalConfig desde BD: " + e.getMessage());
            }
        }
    }
}


package dev.mrraxyer.sportstournamentmanager.config;

import dev.mrraxyer.sportstournamentmanager.models.GlobalProperty;
import dev.mrraxyer.sportstournamentmanager.repositories.GlobalPropertyRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Singleton responsable de cargar y mantener configuraciones globales del sistema/torneo.
 *
 */
public final class GlobalConfig {

    private static volatile GlobalConfig instance;

    private final Properties props = new Properties();

    private GlobalConfig() {
        loadFromClasspath();
    }

    public static GlobalConfig getInstance() {
        if (instance == null) {
            synchronized (GlobalConfig.class) {
                if (instance == null) {
                    instance = new GlobalConfig();
                }
            }
        }
        return instance;
    }

    private void loadFromClasspath() {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            // Si falla la carga desde classpath, dejamos props vacío pero seguimos funcionando
            System.err.println("No se pudo cargar application.properties desde classpath: " + e.getMessage());
        }
    }

    /**
     * Carga (o sobrescribe) propiedades con las almacenadas en base de datos.
     * Si el repositorio es null o vacío, no hace nada.
     */
    public void loadOverridesFromRepository(GlobalPropertyRepository repo) {
        if (repo == null) return;
        List<GlobalProperty> all = repo.findAll();
        if (all == null) return;
        for (GlobalProperty gp : all) {
            if (gp != null && gp.getKey() != null) {
                props.setProperty(gp.getKey(), gp.getValue());
            }
        }
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(props.getProperty(key));
    }

    public String getOrDefault(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public Integer getInt(String key, Integer defaultValue) {
        String v = props.getProperty(key);
        if (v == null) return defaultValue;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        String v = props.getProperty(key);
        if (v == null) return defaultValue;
        return Boolean.parseBoolean(v);
    }

    public Set<String> keySet() {
        return props.stringPropertyNames();
    }

    /**
     * Permite inyectar manualmente o en pruebas un conjunto de propiedades
     */
    public void setProperties(Map<String, String> overrides) {
        if (overrides == null) return;
        for (Map.Entry<String, String> e : overrides.entrySet()) {
            if (e.getKey() != null && e.getValue() != null) props.setProperty(e.getKey(), e.getValue());
        }
    }
}


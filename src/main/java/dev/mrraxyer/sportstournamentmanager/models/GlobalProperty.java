package dev.mrraxyer.sportstournamentmanager.models;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * Entidad para propiedades globales que pueden almacenarse en BD
 */
@Getter
@Entity
@Table(name = "global_properties")
public class GlobalProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "prop_key", nullable = false, unique = true)
    private String key;

    @Column(name = "prop_value")
    private String value;

    public GlobalProperty() {
    }

    public GlobalProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

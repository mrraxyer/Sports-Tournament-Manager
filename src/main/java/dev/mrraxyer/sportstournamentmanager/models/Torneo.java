package dev.mrraxyer.sportstournamentmanager.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/** Entidad Torneo. */
@Entity
@Table(name = "torneos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer torneosId;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(nullable = false, length = 50, name = "tipo_formato")
    private String tipoFormato;

    @Column(nullable = false, name = "fecha_inicio")
    private LocalDate fechaInicio;

    @JsonIgnore
    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Equipo> equipos;

    @JsonIgnore
    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Partido> partidos;

    @JsonIgnore
    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TablaPosiciones> tablasPositiones;

    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'BORRADOR'")
    private String estado = "BORRADOR";

    @Column(name = "num_grupos")
    private Integer numGrupos;

    @PrePersist
    private void prePersist() {
        if (this.estado == null) {
            this.estado = "BORRADOR";
        }
    }
}

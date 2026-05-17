package dev.mrraxyer.sportstournamentmanager.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * Entidad Torneo: Contiene los detalles de configuración para cada torneo.
 * Incluye nombre, tipo de formato y fecha de inicio.
 */
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
    
    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Equipo> equipos;
    
    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Partido> partidos;
    
    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TablaPosiciones> tablasPositiones;
}


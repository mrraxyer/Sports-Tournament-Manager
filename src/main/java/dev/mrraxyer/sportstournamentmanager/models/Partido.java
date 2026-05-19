package dev.mrraxyer.sportstournamentmanager.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad Partido: Registra el calendario y los resultados de los partidos del torneo.
 * Incluye información de equipos participantes, goles y fecha.
 */
@Entity
@Table(name = "partidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Partido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer partidosId;
    
    @ManyToOne
    @JoinColumn(name = "torneos_id", nullable = false)
    private Torneo torneo;
    
    @ManyToOne
    @JoinColumn(name = "id_equipo_local", nullable = false)
    private Equipo equipoLocal;
    
    @ManyToOne
    @JoinColumn(name = "id_equipo_visitante", nullable = false)
    private Equipo equipoVisitante;
    
    @Column(name = "goles_local", nullable = false)
    private Integer golesLocal = 0;

    @Column(name = "goles_visitante", nullable = false)
    private Integer golesVisitante = 0;

    @Column(name = "jugado", nullable = false, columnDefinition = "boolean default false")
    private Boolean jugado = false;

    @Column(nullable = false, name = "fecha_partido")
    private LocalDateTime fechaPartido;
}


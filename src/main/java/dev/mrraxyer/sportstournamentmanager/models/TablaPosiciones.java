package dev.mrraxyer.sportstournamentmanager.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad TablaPosiciones: Rastrea las métricas de rendimiento de los equipos dentro de un torneo específico.
 * Contiene estadísticas como partidos jugados, puntos, goles a favor y en contra.
 */
@Entity
@Table(name = "tabla_posiciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TablaPosiciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tablaPosicionesId;

    @ManyToOne
    @JoinColumn(name = "torneos_id", nullable = false)
    private Torneo torneo;

    @OneToOne
    @JoinColumn(name = "equipos_id", nullable = false)
    private Equipo equipo;

    @Column(nullable = false)
    private Integer partidosJugados = 0;

    @Column(nullable = false)
    private Integer puntos = 0;

    @Column(nullable = false)
    private Integer golesAFavor = 0;

    @Column(nullable = false)
    private Integer golesEnContra = 0;
}


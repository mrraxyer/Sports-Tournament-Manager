package dev.mrraxyer.sportstournamentmanager.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Entidad Jugador: Almacena la información de los jugadores asociados a equipos específicos.
 * Incluye nombre y número de camiseta.
 */
@Entity
@Table(name = "jugadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer jugadoresId;

    @ManyToOne
    @JoinColumn(name = "equipos_id", nullable = false)
    @JsonIgnoreProperties({"jugadores", "partidosLocal", "partidosVisitante", "tablaPosiciones"})
    private Equipo equipo;
    
    @Column(nullable = false, length = 255)
    private String nombre;
    
    @Column(name = "numero_camiseta")
    private Integer numeroCamiseta;
}


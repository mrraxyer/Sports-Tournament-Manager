package dev.mrraxyer.sportstournamentmanager.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Entidad Equipo: Representa los equipos que participan en los torneos.
 * Cada equipo tiene un capitán (usuario) y pertenece a un torneo específico.
 */
@Entity
@Table(name = "equipos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer equiposId;
    
    @ManyToOne
    @JoinColumn(name = "torneos_id")
    private Torneo torneo;
    
    @ManyToOne
    @JoinColumn(name = "id_capitan")
    private Usuario capitan;
    
    @Column(nullable = false, length = 255)
    private String nombre;
    
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Jugador> jugadores;
    
    @OneToMany(mappedBy = "equipoLocal", cascade = CascadeType.ALL)
    private List<Partido> partidosLocal;
    
    @OneToMany(mappedBy = "equipoVisitante", cascade = CascadeType.ALL)
    private List<Partido> partidosVisitante;
    
    @OneToOne(mappedBy = "equipo", cascade = CascadeType.ALL)
    private TablaPosiciones tablaPosiciones;
}


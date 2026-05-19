package dev.mrraxyer.sportstournamentmanager.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/** Entidad Equipo. Pertenece a un torneo y tiene un capitán asignado. */
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
    
    @JsonIgnore
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Jugador> jugadores;

    @JsonIgnore
    @OneToMany(mappedBy = "equipoLocal", cascade = CascadeType.ALL)
    private List<Partido> partidosLocal;

    @JsonIgnore
    @OneToMany(mappedBy = "equipoVisitante", cascade = CascadeType.ALL)
    private List<Partido> partidosVisitante;

    @JsonIgnore
    @OneToOne(mappedBy = "equipo", cascade = CascadeType.ALL)
    private TablaPosiciones tablaPosiciones;
}


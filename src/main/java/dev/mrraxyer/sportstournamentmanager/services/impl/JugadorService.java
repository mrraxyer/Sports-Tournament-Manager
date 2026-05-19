package dev.mrraxyer.sportstournamentmanager.services.impl;

import dev.mrraxyer.sportstournamentmanager.models.Jugador;
import dev.mrraxyer.sportstournamentmanager.repositories.BaseRepository;
import dev.mrraxyer.sportstournamentmanager.repositories.JugadorRepository;
import dev.mrraxyer.sportstournamentmanager.services.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Servicio de Jugador. */
@Service
public class JugadorService extends BaseService<Jugador, Integer> {

    @Autowired
    private JugadorRepository jugadorRepository;

    @Override
    protected BaseRepository<Jugador, Integer> getRepository() {
        return jugadorRepository;
    }
}

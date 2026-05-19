package dev.mrraxyer.sportstournamentmanager.controllers;

import dev.mrraxyer.sportstournamentmanager.dto.ApiResponse;
import dev.mrraxyer.sportstournamentmanager.dto.ApiResponseBuilder;
import dev.mrraxyer.sportstournamentmanager.models.Torneo;
import dev.mrraxyer.sportstournamentmanager.services.impl.TorneoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.mrraxyer.sportstournamentmanager.services.MatchSchedulerService;
import dev.mrraxyer.sportstournamentmanager.services.impl.PartidoService;
import dev.mrraxyer.sportstournamentmanager.models.Partido;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Controlador de Torneos. */
@RestController
@RequestMapping("/api/torneos")
public class TorneoController {

    @Autowired
    private TorneoService torneoService;

    @Autowired
    private MatchSchedulerService matchSchedulerService;

    @Autowired
    private PartidoService partidoService;

    @Autowired
    private dev.mrraxyer.sportstournamentmanager.repositories.EquipoRepository equipoRepository;
    @Autowired
    private dev.mrraxyer.sportstournamentmanager.repositories.TablaPosicionesRepository tablaPosicionesRepository;
    @Autowired
    private dev.mrraxyer.sportstournamentmanager.repositories.PartidoRepository partidoRepository;
    @Autowired
    private dev.mrraxyer.sportstournamentmanager.services.impl.TablaPosicionesService tablaPosicionesService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Torneo>> obtenerTorneo(@PathVariable Integer id) {
        Optional<Torneo> torneo = torneoService.findById(id);

        if (torneo.isPresent()) {
            ApiResponse<Torneo> response = ApiResponseBuilder
                    .success(torneo.get())
                    .message("Torneo encontrado")
                    .path("/api/torneos/" + id)
                    .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Torneo> response = ApiResponseBuilder
                    .<Torneo>error("Torneo no encontrado", HttpStatus.NOT_FOUND.value())
                    .path("/api/torneos/" + id)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/{id}/equipos")
    public ResponseEntity<ApiResponse<java.util.List<dev.mrraxyer.sportstournamentmanager.models.Equipo>>> listarEquiposPorTorneo(
            @PathVariable Integer id) {
        Optional<Torneo> torneoOpt = torneoService.findById(id);

        if (!torneoOpt.isPresent()) {
            ApiResponse<java.util.List<dev.mrraxyer.sportstournamentmanager.models.Equipo>> response = ApiResponseBuilder.<java.util.List<dev.mrraxyer.sportstournamentmanager.models.Equipo>>error(
                    "Torneo no encontrado", HttpStatus.NOT_FOUND.value())
                    .path("/api/torneos/" + id + "/equipos")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        java.util.List<dev.mrraxyer.sportstournamentmanager.models.Equipo> equipos = equipoRepository
                .findByTorneo(torneoOpt.get());

        ApiResponse<java.util.List<dev.mrraxyer.sportstournamentmanager.models.Equipo>> response = ApiResponseBuilder
                .success(equipos)
                .message("Equipos del torneo: " + equipos.size())
                .path("/api/torneos/" + id + "/equipos")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Torneo>>> listarTorneos() {
        List<Torneo> torneos = torneoService.findAll();

        ApiResponse<List<Torneo>> response = ApiResponseBuilder
                .success(torneos)
                .message("Total de torneos: " + torneos.size())
                .path("/api/torneos")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Torneo>> crearTorneo(@RequestBody Torneo torneo) {
        Torneo torneoGuardado = torneoService.save(torneo);

        ApiResponse<Torneo> response = ApiResponseBuilder
                .created(torneoGuardado)
                .message("Torneo creado exitosamente")
                .path("/api/torneos")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Torneo>> actualizarTorneo(
            @PathVariable Integer id,
            @RequestBody Torneo torneoActualizado) {

        Optional<Torneo> torneoExistente = torneoService.findById(id);

        if (torneoExistente.isPresent()) {
            torneoActualizado.setTorneosId(id);
            Torneo torneoGuardado = torneoService.save(torneoActualizado);

            ApiResponse<Torneo> response = ApiResponseBuilder
                    .success(torneoGuardado)
                    .message("Torneo actualizado exitosamente")
                    .path("/api/torneos/" + id)
                    .build();
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Torneo> response = ApiResponseBuilder
                    .<Torneo>error("Torneo no encontrado", HttpStatus.NOT_FOUND.value())
                    .path("/api/torneos/" + id)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTorneo(@PathVariable Integer id) {
        if (torneoService.existsById(id)) {
            torneoService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/buscar/nombre")
    public ResponseEntity<ApiResponse<List<Torneo>>> buscarPorNombre(
            @RequestParam String nombre) {

        List<Torneo> torneos = torneoService.findByNombre(nombre);

        ApiResponse<List<Torneo>> response = ApiResponseBuilder
                .success(torneos)
                .message("Torneos encontrados: " + torneos.size())
                .path("/api/torneos/buscar/nombre")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscar/formato")
    public ResponseEntity<ApiResponse<List<Torneo>>> buscarPorFormato(
            @RequestParam String tipoFormato) {

        List<Torneo> torneos = torneoService.findByTipoFormato(tipoFormato);

        ApiResponse<List<Torneo>> response = ApiResponseBuilder
                .success(torneos)
                .message("Torneos encontrados: " + torneos.size())
                .path("/api/torneos/buscar/formato")
                .build();
        return ResponseEntity.ok(response);
    }

    /** Genera calendario según el formato del torneo. Retorna 409 si ya existen partidos. */
    @PostMapping("/{id}/generar-calendario")
    public ResponseEntity<ApiResponse<List<Partido>>> generarCalendario(@PathVariable Integer id) {
        Optional<Torneo> torneoOpt = torneoService.findById(id);

        if (!torneoOpt.isPresent()) {
            ApiResponse<List<Partido>> response = ApiResponseBuilder
                    .<List<Partido>>error("Torneo no encontrado", HttpStatus.NOT_FOUND.value())
                    .path("/api/torneos/" + id + "/generar-calendario")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Torneo torneo = torneoOpt.get();

        List<Partido> existing = partidoService.findByTorneo(torneo);

        if (!existing.isEmpty()) {
            ApiResponse<List<Partido>> response = ApiResponseBuilder
                    .<List<Partido>>error("Este torneo ya tiene partidos generados", HttpStatus.CONFLICT.value())
                    .path("/api/torneos/" + id + "/generar-calendario")
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        List<Partido> partidos = matchSchedulerService.scheduleMatchesUsingTorneoFormat(torneo, LocalDate.now());

        ApiResponse<List<Partido>> response = ApiResponseBuilder
                .created(partidos)
                .message("Calendario generado exitosamente: " + partidos.size() + " partidos")
                .path("/api/torneos/" + id + "/generar-calendario")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/avanzar-eliminatoria")
    public ResponseEntity<ApiResponse<List<Partido>>> avanzarEliminatoria(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "2") Integer clasificadosPorGrupo) {

        Optional<Torneo> torneoOpt = torneoService.findById(id);
        if (!torneoOpt.isPresent()) {
            ApiResponse<List<Partido>> response = ApiResponseBuilder
                    .<List<Partido>>error("Torneo no encontrado", HttpStatus.NOT_FOUND.value())
                    .path("/api/torneos/" + id + "/avanzar-eliminatoria")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Torneo torneo = torneoOpt.get();

        String tipo = torneo.getTipoFormato() == null ? "" : torneo.getTipoFormato().toLowerCase();
        if (!(tipo.contains("grupos") || tipo.contains("grupo"))) {
            ApiResponse<List<Partido>> response = ApiResponseBuilder
                    .<List<Partido>>error("El torneo no está en formato GRUPOS", HttpStatus.CONFLICT.value())
                    .path("/api/torneos/" + id + "/avanzar-eliminatoria")
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        Integer numGrupos = torneo.getNumGrupos() == null ? 2 : torneo.getNumGrupos();
        List<dev.mrraxyer.sportstournamentmanager.models.Equipo> clasificados = new java.util.ArrayList<>();
        char letra = 'A';
        for (int g = 0; g < numGrupos; g++) {
            String grupoCodigo = String.valueOf(letra);
            List<dev.mrraxyer.sportstournamentmanager.models.TablaPosiciones> tps = tablaPosicionesRepository
                    .findByTorneoAndGrupo(torneo, grupoCodigo);

            tps.sort((a, b) -> {
                int cmp = Integer.compare(b.getPuntos(), a.getPuntos());
                if (cmp != 0)
                    return cmp;
                int difA = a.getGolesAFavor() - a.getGolesEnContra();
                int difB = b.getGolesAFavor() - b.getGolesEnContra();
                if (difA != difB)
                    return Integer.compare(difB, difA);
                try {
                    List<dev.mrraxyer.sportstournamentmanager.models.Partido> h2h = partidoRepository
                            .findHeadToHead(torneo, a.getEquipo(), b.getEquipo());
                    int puntosA = 0, puntosB = 0;
                    int golesA = 0, golesB = 0;
                    for (dev.mrraxyer.sportstournamentmanager.models.Partido p : h2h) {
                        if (p.getEquipoLocal().getEquiposId().equals(a.getEquipo().getEquiposId())) {
                            int gA = p.getGolesLocal();
                            int gB = p.getGolesVisitante();
                            golesA += gA;
                            golesB += gB;
                            if (gA > gB)
                                puntosA += 3;
                            else if (gA == gB) {
                                puntosA += 1;
                                puntosB += 1;
                            } else
                                puntosB += 3;
                        } else {
                            int gA = p.getGolesVisitante();
                            int gB = p.getGolesLocal();
                            golesA += gA;
                            golesB += gB;
                            if (gA > gB)
                                puntosA += 3;
                            else if (gA == gB) {
                                puntosA += 1;
                                puntosB += 1;
                            } else
                                puntosB += 3;
                        }
                    }
                    if (puntosA != puntosB)
                        return Integer.compare(puntosB, puntosA);
                    if (golesA != golesB)
                        return Integer.compare(golesB, golesA);
                } catch (Exception ex) {
                }
                return Integer.compare(b.getGolesAFavor(), a.getGolesAFavor());
            });

            for (int i = 0; i < Math.min(clasificadosPorGrupo, tps.size()); i++) {
                clasificados.add(tps.get(i).getEquipo());
            }

            letra++;
        }

        dev.mrraxyer.sportstournamentmanager.strategies.MatchScheduleStrategy elim = new dev.mrraxyer.sportstournamentmanager.strategies.SingleEliminationScheduleStrategy();
        List<Partido> partidosElim = matchSchedulerService.scheduleMatches(torneo, clasificados, elim,
                java.time.LocalDate.now());

        for (Partido p : partidosElim) {
            p.setGrupo(null);
        }
        partidoService.saveAll(partidosElim);

        ApiResponse<List<Partido>> response = ApiResponseBuilder
                .created(partidosElim)
                .message("Eliminatoria generada con " + partidosElim.size() + " partidos")
                .path("/api/torneos/" + id + "/avanzar-eliminatoria")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Torneo>> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String estado) {

        Optional<Torneo> torneoOpt = torneoService.findById(id);

        if (!torneoOpt.isPresent()) {
            ApiResponse<Torneo> response = ApiResponseBuilder
                    .<Torneo>error("Torneo no encontrado", HttpStatus.NOT_FOUND.value())
                    .path("/api/torneos/" + id + "/estado")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Torneo torneo = torneoOpt.get();
        String actual = torneo.getEstado();
        String nuevo = estado != null ? estado.toUpperCase() : null;

        if (nuevo == null
                || !("BORRADOR".equals(nuevo) || "ACTIVO".equals(nuevo) || "FINALIZADO".equals(nuevo))) {
            ApiResponse<Torneo> response = ApiResponseBuilder
                    .<Torneo>error("Estado inválido", HttpStatus.BAD_REQUEST.value())
                    .path("/api/torneos/" + id + "/estado")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if ("BORRADOR".equalsIgnoreCase(actual) && "ACTIVO".equals(nuevo)) {
        } else if ("ACTIVO".equalsIgnoreCase(actual) && "FINALIZADO".equals(nuevo)) {
        } else if (actual.equalsIgnoreCase(nuevo)) {
        } else {
            ApiResponse<Torneo> response = ApiResponseBuilder
                    .<Torneo>error("Transición de estado inválida", HttpStatus.CONFLICT.value())
                    .path("/api/torneos/" + id + "/estado")
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        torneo.setEstado(nuevo);
        Torneo guardado = torneoService.save(torneo);

        ApiResponse<Torneo> response = ApiResponseBuilder
                .success(guardado)
                .message("Estado actualizado a " + nuevo)
                .path("/api/torneos/" + id + "/estado")
                .build();
        return ResponseEntity.ok(response);
    }
}

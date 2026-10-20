package co.edu.unicauca.tracking.service;

import co.edu.unicauca.tracking.controller.ApiException;
import co.edu.unicauca.tracking.dto.Dtos.ResumenDto;
import co.edu.unicauca.tracking.dto.EventEnvelope;
import co.edu.unicauca.tracking.model.ProcessedEvent;
import co.edu.unicauca.tracking.model.UserStats;
import co.edu.unicauca.tracking.repository.ProcessedEventRepository;
import co.edu.unicauca.tracking.repository.UserStatsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrackingService {

    private final UserStatsRepository estadisticas;
    private final ProcessedEventRepository procesados;

    public TrackingService(UserStatsRepository estadisticas, ProcessedEventRepository procesados) {
        this.estadisticas = estadisticas;
        this.procesados = procesados;
    }

    /** Suma un simulacro resuelto a las estadísticas del usuario (idempotente por `eventId`). */
    @Transactional
    public void registrar(EventEnvelope e) {
        if (!"SimulationCompleted".equals(e.type()) || e.eventId() == null || procesados.existsById(e.eventId())) {
            return;
        }
        Long userId = e.longOf("userId");
        Object correctas = e.data().get("correct");
        Object total = e.data().get("total");
        Object puntaje = e.data().get("score");
        if (userId == null || !(correctas instanceof Number) || !(total instanceof Number) || !(puntaje instanceof Number)) {
            return;
        }
        UserStats stats = estadisticas.findById(userId).orElseGet(() -> new UserStats(userId));
        stats.sumar(((Number) correctas).intValue(), ((Number) total).intValue(), ((Number) puntaje).doubleValue());
        estadisticas.save(stats);
        procesados.save(new ProcessedEvent(e.eventId()));
    }

    public UserStats deUsuario(Long userId) {
        return estadisticas.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El usuario " + userId + " no tiene simulacros registrados"));
    }

    public ResumenDto resumen() {
        List<UserStats> todos = estadisticas.findAll();
        int simulacros = todos.stream().mapToInt(UserStats::getSimulacros).sum();
        double suma = todos.stream().mapToDouble(UserStats::getSumaPuntajes).sum();
        return new ResumenDto(todos.size(), simulacros, simulacros == 0 ? 0 : Math.round(10.0 * suma / simulacros) / 10.0);
    }
}

package co.edu.unicauca.history.service;

import co.edu.unicauca.history.dto.EventEnvelope;
import co.edu.unicauca.history.model.Entrada;
import co.edu.unicauca.history.repository.EntradaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/** Guarda cada evento del sistema como una entrada del historial (idempotente por `eventId`). */
@Service
public class HistoryService {

    private final EntradaRepository entradas;
    private final ObjectMapper json;

    public HistoryService(EntradaRepository entradas, ObjectMapper json) {
        this.entradas = entradas;
        this.json = json;
    }

    public void registrar(EventEnvelope e) {
        if (e.eventId() == null || entradas.existsByEventId(e.eventId())) {
            return;
        }
        entradas.save(new Entrada(e.eventId(), e.type(), e.longOf("questionId"), actorDe(e), resumenDe(e), detallesDe(e), instante(e)));
    }

    public List<Entrada> lineaDeTiempo(Long questionId) {
        return entradas.findByQuestionIdOrderByIdAsc(questionId);
    }

    public Page<Entrada> auditoria(String tipo, int pagina, int tamano) {
        String filtro = tipo == null || tipo.isBlank() ? null : tipo;
        return entradas.auditoria(filtro, PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), 100)));
    }

    private static String actorDe(EventEnvelope e) {
        for (String clave : List.of("changedBy", "assignedBy", "reviewerId", "userId", "username", "authorId")) {
            String valor = e.stringOf(clave);
            if (valor != null) {
                return valor;
            }
        }
        return null;
    }

    private static String resumenDe(EventEnvelope e) {
        return switch (e.type()) {
            case "question.state-changed" -> "Cambió de " + e.stringOf("from") + " a " + e.stringOf("to");
            case "QuestionSubmittedForReview" -> "La pregunta se envió a revisión";
            case "question.published" -> "La pregunta se publicó";
            case "ValidationFailed" -> "La validación falló: " + e.data().get("errors");
            case "ReviewerAssigned" -> "Se asignó al revisor " + e.stringOf("reviewerId") + " (ronda " + e.data().get("round") + ")";
            case "EvaluationSubmitted" -> "El revisor " + e.stringOf("reviewerId") + " evaluó: " + e.stringOf("decision");
            case "ReviewCompleted" -> "La revisión terminó: " + e.stringOf("result");
            case "auth.login-succeeded" -> "Inicio de sesión exitoso de " + e.stringOf("username");
            case "auth.login-failed" -> "Inicio de sesión fallido de " + e.stringOf("username");
            case "SimulationCompleted" -> "El usuario " + e.stringOf("userId") + " resolvió un simulacro";
            default -> e.type();
        };
    }

    private String detallesDe(EventEnvelope e) {
        try {
            return json.writeValueAsString(e.data());
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private static Instant instante(EventEnvelope e) {
        try {
            return Instant.parse(e.occurredAt());
        } catch (RuntimeException ex) {
            return Instant.now();
        }
    }
}

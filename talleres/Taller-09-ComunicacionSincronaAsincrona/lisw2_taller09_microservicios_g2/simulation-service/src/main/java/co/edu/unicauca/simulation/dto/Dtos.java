package co.edu.unicauca.simulation.dto;

import co.edu.unicauca.simulation.model.CatalogQuestion;
import co.edu.unicauca.simulation.model.Simulacro;
import co.edu.unicauca.simulation.model.SimulacroEstado;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Contratos REST del servicio de simulacros. */
public final class Dtos {

    private Dtos() {
    }

    public record CrearSimulacroRequest(@NotNull Long userId, @Min(1) int cantidad) {
    }

    public record RespuestaItem(@NotNull Long questionId, @NotBlank String respuesta) {
    }

    public record ResolverRequest(@NotNull List<RespuestaItem> respuestas) {
    }

    /** Pregunta tal como la ve quien resuelve el simulacro: sin la respuesta correcta. */
    public record PreguntaSimulacroDto(Long questionId, String titulo, String competencia, String contexto, String pregunta,
                                       List<String> opciones) {
        public static PreguntaSimulacroDto de(CatalogQuestion q) {
            return new PreguntaSimulacroDto(q.getQuestionId(), q.getTitulo(), q.getCompetencia(), q.getContexto(), q.getPregunta(), q.getOpciones());
        }
    }

    public record CatalogoDto(Long questionId, String titulo, String competencia) {
        public static CatalogoDto de(CatalogQuestion q) {
            return new CatalogoDto(q.getQuestionId(), q.getTitulo(), q.getCompetencia());
        }
    }

    public record SimulacroDto(Long id, Long userId, SimulacroEstado estado, List<PreguntaSimulacroDto> preguntas) {
        public static SimulacroDto de(Simulacro s, List<CatalogQuestion> preguntas) {
            return new SimulacroDto(s.getId(), s.getUserId(), s.getEstado(), preguntas.stream().map(PreguntaSimulacroDto::de).toList());
        }
    }

    public record ResultadoDto(Long simulationId, Long userId, int correctas, int total, double puntaje) {
    }

    /** Lo que el servicio de preguntas devuelve de una pregunta publicada. */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record PreguntaRemota(Long id, String titulo, String contexto, String pregunta, List<String> opciones,
                                 String respuestaCorrecta, String competencia) {
    }
}

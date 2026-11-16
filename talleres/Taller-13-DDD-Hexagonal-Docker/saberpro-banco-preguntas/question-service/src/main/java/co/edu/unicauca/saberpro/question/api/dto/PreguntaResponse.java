package co.edu.unicauca.saberpro.question.api.dto;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import java.util.List;

public record PreguntaResponse(String id, String enunciado, String justificacion, String estado,
                               List<OpcionResponse> opciones) {

    public record OpcionResponse(String texto, boolean correcta) {
    }

    public static PreguntaResponse de(Pregunta p) {
        return new PreguntaResponse(p.id(), p.enunciado().texto(), p.justificacion().texto(), p.estado().name(),
                p.opciones().stream().map(o -> new OpcionResponse(o.texto(), o.esCorrecta())).toList());
    }
}

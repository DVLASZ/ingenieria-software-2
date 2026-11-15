package co.edu.unicauca.saberpro.question.domain.ports.inbound;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import java.util.List;

/** Puerto de entrada: crear una pregunta. */
public interface CrearPreguntaUseCase {

    Pregunta crear(CrearPreguntaComando comando);

    record CrearPreguntaComando(String enunciado, String justificacion, List<OpcionComando> opciones) {
    }

    record OpcionComando(String texto, boolean correcta) {
    }
}

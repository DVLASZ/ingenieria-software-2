package co.edu.unicauca.saberpro.domain.ports.inbound;

import co.edu.unicauca.saberpro.domain.model.Pregunta;
import java.util.List;

/** Puerto de entrada: lo que el mundo de afuera puede pedirle al banco sobre la creación de preguntas. */
public interface CrearPreguntaUseCase {

    Pregunta crear(CrearPreguntaComando comando);

    Pregunta enviarARevision(String id);

    record CrearPreguntaComando(String enunciado, String justificacion, List<OpcionComando> opciones) {
    }

    record OpcionComando(String texto, boolean correcta) {
    }
}

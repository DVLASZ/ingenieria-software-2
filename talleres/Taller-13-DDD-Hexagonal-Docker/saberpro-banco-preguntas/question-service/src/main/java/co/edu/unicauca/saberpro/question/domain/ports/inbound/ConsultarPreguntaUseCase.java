package co.edu.unicauca.saberpro.question.domain.ports.inbound;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import java.util.List;

public interface ConsultarPreguntaUseCase {

    Pregunta consultar(String id);

    List<Pregunta> listar();
}

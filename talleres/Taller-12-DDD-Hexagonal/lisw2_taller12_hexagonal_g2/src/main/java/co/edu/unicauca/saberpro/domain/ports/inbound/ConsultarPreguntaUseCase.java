package co.edu.unicauca.saberpro.domain.ports.inbound;

import co.edu.unicauca.saberpro.domain.model.Pregunta;

public interface ConsultarPreguntaUseCase {

    Pregunta consultar(String id);
}

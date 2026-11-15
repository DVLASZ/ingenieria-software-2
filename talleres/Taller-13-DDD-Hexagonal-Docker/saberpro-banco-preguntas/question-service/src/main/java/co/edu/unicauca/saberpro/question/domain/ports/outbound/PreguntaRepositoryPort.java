package co.edu.unicauca.saberpro.question.domain.ports.outbound;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: guardar y buscar preguntas sin saber dónde viven. */
public interface PreguntaRepositoryPort {

    Pregunta save(Pregunta pregunta);

    Optional<Pregunta> findById(String id);

    List<Pregunta> findAll();
}

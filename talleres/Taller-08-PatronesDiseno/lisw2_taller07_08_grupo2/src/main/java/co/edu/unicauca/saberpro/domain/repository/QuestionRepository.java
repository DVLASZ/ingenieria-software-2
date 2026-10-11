package co.edu.unicauca.saberpro.domain.repository;

import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.List;
import java.util.Optional;

/** Puerto de salida del dominio: dónde se guardan las preguntas del banco. */
public interface QuestionRepository {

    /** Guarda la pregunta y la devuelve. */
    Question save(Question question);

    Optional<Question> findById(String id);

    /** Todas las preguntas, en el orden en que se guardaron. */
    List<Question> findAll();
}

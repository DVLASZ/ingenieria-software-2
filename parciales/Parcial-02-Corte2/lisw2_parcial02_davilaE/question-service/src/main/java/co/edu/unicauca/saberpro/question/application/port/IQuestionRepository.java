package co.edu.unicauca.saberpro.question.application.port;

import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.Optional;

public interface IQuestionRepository {

    /** inserta si no tiene id, actualiza si ya lo tiene. Devuelve la pregunta con su id */
    Question save(Question question);

    Optional<Question> findById(Long id);
}

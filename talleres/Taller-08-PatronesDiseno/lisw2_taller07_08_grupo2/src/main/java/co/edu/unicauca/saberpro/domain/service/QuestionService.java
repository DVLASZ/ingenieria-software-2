package co.edu.unicauca.saberpro.domain.service;

import co.edu.unicauca.saberpro.adapter.QuestionProvider;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.domain.repository.QuestionRepository;
import co.edu.unicauca.saberpro.domain.validation.QuestionValidator;

import java.util.List;
import java.util.Optional;

/** Casos de uso del banco de preguntas: registrar (validando), consultar e importar desde una fuente. */
public class QuestionService {

    private final QuestionRepository repository;
    private final QuestionValidator validator;

    public QuestionService(QuestionRepository repository, QuestionValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    /**
     * Valida y guarda la pregunta.
     *
     * @throws IllegalArgumentException con los incumplimientos si la pregunta no es válida
     */
    public Question register(Question question) {
        List<String> violations = validator.validate(question);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Pregunta rechazada: " + String.join("; ", violations));
        }
        return repository.save(question);
    }

    /**
     * Registra las preguntas que entrega una fuente; las que no pasan la validación se omiten.
     *
     * @return cuántas preguntas se registraron
     */
    public int importFrom(QuestionProvider provider) {
        int imported = 0;
        for (Question question : provider.getQuestions()) {
            if (validator.isValid(question)) {
                repository.save(question);
                imported++;
            }
        }
        return imported;
    }

    public Optional<Question> findById(String id) {
        return repository.findById(id);
    }

    public List<Question> findAll() {
        return repository.findAll();
    }
}

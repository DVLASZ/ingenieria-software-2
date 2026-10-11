package co.edu.unicauca.saberpro.domain.repository;

import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Repositorio en memoria: suficiente para el taller, sin base de datos. */
public class InMemoryQuestionRepository implements QuestionRepository {

    private final Map<String, Question> questions = new LinkedHashMap<>();

    @Override
    public Question save(Question question) {
        questions.put(question.getId(), question);
        return question;
    }

    @Override
    public Optional<Question> findById(String id) {
        return Optional.ofNullable(questions.get(id));
    }

    @Override
    public List<Question> findAll() {
        return new ArrayList<>(questions.values());
    }
}

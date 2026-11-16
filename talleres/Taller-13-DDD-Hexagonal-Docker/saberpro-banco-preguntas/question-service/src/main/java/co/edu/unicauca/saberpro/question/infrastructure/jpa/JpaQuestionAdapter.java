package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import java.util.List;
import java.util.Optional;

public class JpaQuestionAdapter implements PreguntaRepositoryPort {

    private final SpringQuestionDataRepository repositorio;
    private final QuestionDomainMapper mapper;

    public JpaQuestionAdapter(SpringQuestionDataRepository repositorio, QuestionDomainMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Pregunta save(Pregunta pregunta) {
        return mapper.toDomain(repositorio.save(mapper.toRecord(pregunta)));
    }

    @Override
    public Optional<Pregunta> findById(String id) {
        return repositorio.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Pregunta> findAll() {
        return repositorio.findAll().stream().map(mapper::toDomain).toList();
    }
}

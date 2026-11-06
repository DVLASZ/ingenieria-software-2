package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.question.application.port.IQuestionRepository;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.mapper.QuestionDomainMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** adaptador del puerto IQuestionRepository sobre Spring Data JPA (H2) */
@Repository
@Transactional
public class QuestionRepositoryJpaImpl implements IQuestionRepository {

    private final SpringQuestionDataRepository dataRepository;
    private final QuestionDomainMapper mapper;

    public QuestionRepositoryJpaImpl(SpringQuestionDataRepository dataRepository, QuestionDomainMapper mapper) {
        this.dataRepository = dataRepository;
        this.mapper = mapper;
    }

    @Override
    public Question save(Question question) {
        return mapper.toDomain(dataRepository.save(mapper.toRecord(question)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Question> findById(Long id) {
        return dataRepository.findById(id).map(mapper::toDomain);
    }
}

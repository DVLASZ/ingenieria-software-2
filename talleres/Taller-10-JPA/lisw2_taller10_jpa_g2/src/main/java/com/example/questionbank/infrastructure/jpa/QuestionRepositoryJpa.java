package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.exception.NotFoundException;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.infrastructure.jpa.mapper.QuestionMapper;
import com.example.questionbank.infrastructure.jpa.record.CompetencyRecord;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.TopicRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringCompetencyDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuestionDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringTopicDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stores questions of every type with Spring Data JPA. The whole class runs inside transactions, so the lazy
 * associations can be read while the mapper builds the domain objects.
 */
@Repository
@Transactional(readOnly = true)
public class QuestionRepositoryJpa implements QuestionRepository {

    private final SpringQuestionDataRepository questions;
    private final SpringUserDataRepository users;
    private final SpringTopicDataRepository topics;
    private final SpringCompetencyDataRepository competencies;

    /**
     * Creates the repository.
     *
     * @param questions Spring Data interface for question records
     * @param users Spring Data interface for user records
     * @param topics Spring Data interface for topic records
     * @param competencies Spring Data interface for competency records
     */
    public QuestionRepositoryJpa(SpringQuestionDataRepository questions, SpringUserDataRepository users,
                                 SpringTopicDataRepository topics, SpringCompetencyDataRepository competencies) {
        this.questions = questions;
        this.users = users;
        this.topics = topics;
        this.competencies = competencies;
    }

    @Override
    @Transactional
    public Question save(Question question) {
        TopicRecord topic = resolveTopic(question);
        Set<CompetencyRecord> competencyRecords = resolveCompetencies(question);
        QuestionRecord saved;
        if (question.getId() == null) {
            UserRecord author = resolveAuthor(question);
            saved = questions.save(QuestionMapper.toNewRecord(question, author, topic, competencyRecords));
        } else {
            QuestionRecord record = questions.findById(question.getId())
                    .orElseThrow(() -> new NotFoundException("Question not found: " + question.getId()));
            QuestionMapper.copyInto(record, question, topic, competencyRecords);
            saved = questions.save(record);
        }
        return QuestionMapper.toDomain(saved);
    }

    @Override
    public Optional<Question> findById(Long id) {
        return questions.findById(id).map(QuestionMapper::toDomain);
    }

    @Override
    public List<Question> findAllById(Collection<Long> ids) {
        return questions.findAllById(ids).stream().map(QuestionMapper::toDomain).toList();
    }

    @Override
    public List<Question> findAll() {
        return questions.findAll(Sort.by("id")).stream().map(QuestionMapper::toDomain).toList();
    }

    @Override
    public List<Question> findByAuthorId(Long authorId) {
        return questions.findByAuthorIdOrderByIdAsc(authorId).stream().map(QuestionMapper::toDomain).toList();
    }

    @Override
    public List<Question> findByStatus(QuestionStatus status) {
        return questions.findByStatusOrderByIdAsc(status).stream().map(QuestionMapper::toDomain).toList();
    }

    @Override
    public List<Question> findPublishedByCompetencyId(Long competencyId) {
        return questions.findByCompetencyIdAndStatus(competencyId, QuestionStatus.PUBLISHED).stream()
                .map(QuestionMapper::toDomain).toList();
    }

    private UserRecord resolveAuthor(Question question) {
        Long authorId = question.getAuthor().getId();
        if (authorId == null) {
            throw new NotFoundException("The author has not been saved yet");
        }
        return users.findById(authorId).orElseThrow(() -> new NotFoundException("Author not found: " + authorId));
    }

    private TopicRecord resolveTopic(Question question) {
        if (question.getTopic() == null) {
            return null;
        }
        Long topicId = question.getTopic().getId();
        if (topicId == null) {
            throw new NotFoundException("The topic has not been saved yet");
        }
        return topics.findById(topicId).orElseThrow(() -> new NotFoundException("Topic not found: " + topicId));
    }

    private Set<CompetencyRecord> resolveCompetencies(Question question) {
        Set<Long> ids = question.getCompetencies().stream().map(Competency::getId).collect(Collectors.toSet());
        if (ids.contains(null)) {
            throw new NotFoundException("A competency has not been saved yet");
        }
        List<CompetencyRecord> found = competencies.findAllById(ids);
        if (found.size() != ids.size()) {
            throw new NotFoundException("Some competencies do not exist: " + ids);
        }
        return Set.copyOf(found);
    }
}

package com.example.questionbank.application;

import com.example.questionbank.application.command.CreateMultipleChoiceQuestionCommand;
import com.example.questionbank.application.command.CreateTrueFalseQuestionCommand;
import com.example.questionbank.domain.exception.BusinessRuleException;
import com.example.questionbank.domain.exception.NotFoundException;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.CompetencyRepository;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.TopicRepository;
import com.example.questionbank.domain.repository.UserRepository;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases about questions: create, query, classify, send to review and move through the life cycle.
 * The rules live in the domain; this class only loads the objects, calls the domain and stores the result.
 */
@Service
@Transactional
public class QuestionService {

    private final QuestionRepository questions;
    private final UserRepository users;
    private final TopicRepository topics;
    private final CompetencyRepository competencies;

    /**
     * Creates the service.
     *
     * @param questions the question repository contract
     * @param users the user repository contract
     * @param topics the topic repository contract
     * @param competencies the competency repository contract
     */
    public QuestionService(QuestionRepository questions, UserRepository users, TopicRepository topics,
                           CompetencyRepository competencies) {
        this.questions = questions;
        this.users = users;
        this.topics = topics;
        this.competencies = competencies;
    }

    /**
     * Creates a multiple choice draft.
     *
     * @param command the data of the question
     * @return the stored question
     */
    public Question createMultipleChoice(CreateMultipleChoiceQuestionCommand command) {
        User author = requireAuthor(command.authorId());
        MultipleChoiceQuestion question = new MultipleChoiceQuestion(null, command.statement(),
                command.difficulty(), author, command.options());
        return storeNew(question, command.metadata());
    }

    /**
     * Creates a true/false draft.
     *
     * @param command the data of the question
     * @return the stored question
     */
    public Question createTrueFalse(CreateTrueFalseQuestionCommand command) {
        User author = requireAuthor(command.authorId());
        TrueFalseQuestion question = new TrueFalseQuestion(null, command.statement(), command.difficulty(),
                author, command.expectedAnswer());
        return storeNew(question, command.metadata());
    }

    /**
     * Finds a question.
     *
     * @param id the identity
     * @return the question
     */
    @Transactional(readOnly = true)
    public Question findById(Long id) {
        return questions.findById(id).orElseThrow(() -> new NotFoundException("Question not found: " + id));
    }

    /**
     * Lists questions by author, by status, by both, or all of them when no filter is given.
     *
     * @param authorId identity of the author, or null
     * @param status status, or null
     * @return the questions that match
     */
    @Transactional(readOnly = true)
    public List<Question> list(Long authorId, QuestionStatus status) {
        if (authorId != null && status != null) {
            return questions.findByAuthorId(authorId).stream().filter(question -> question.getStatus() == status)
                    .toList();
        }
        if (authorId != null) {
            return questions.findByAuthorId(authorId);
        }
        if (status != null) {
            return questions.findByStatus(status);
        }
        return questions.findAll();
    }

    /**
     * Lists the published questions that evaluate a competency.
     *
     * @param competencyId identity of the competency
     * @return the published questions
     */
    @Transactional(readOnly = true)
    public List<Question> listPublishedByCompetency(Long competencyId) {
        competencies.findById(competencyId)
                .orElseThrow(() -> new NotFoundException("Competency not found: " + competencyId));
        return questions.findPublishedByCompetencyId(competencyId);
    }

    /**
     * Gives a question a topic and one or more competencies.
     *
     * @param questionId identity of the question
     * @param topicId identity of the topic
     * @param competencyIds identities of the competencies
     * @return the stored question
     */
    public Question classify(Long questionId, Long topicId, Collection<Long> competencyIds) {
        Question question = findById(questionId);
        Topic topic = topics.findById(topicId).orElseThrow(() -> new NotFoundException("Topic not found: " + topicId));
        Set<Long> ids = new HashSet<>(competencyIds);
        List<Competency> found = competencies.findAllById(ids);
        if (found.size() != ids.size()) {
            throw new NotFoundException("Some competencies do not exist: " + ids);
        }
        question.classify(topic, found);
        return questions.save(question);
    }

    /**
     * Sends a draft to review. The domain checks the minimum conditions; if they fail nothing is stored.
     *
     * @param questionId identity of the question
     * @return the stored question
     */
    public Question submitForReview(Long questionId) {
        Question question = findById(questionId);
        question.submitForReview();
        return questions.save(question);
    }

    /**
     * Moves a question to another status, if the life cycle allows it.
     *
     * @param questionId identity of the question
     * @param target the new status
     * @return the stored question
     */
    public Question changeStatus(Long questionId, QuestionStatus target) {
        Question question = findById(questionId);
        question.moveTo(target);
        return questions.save(question);
    }

    private User requireAuthor(Long authorId) {
        User author = users.findById(authorId).orElseThrow(() -> new NotFoundException("Author not found: " + authorId));
        if (author.getRole() != Role.AUTHOR) {
            throw new BusinessRuleException("Only users with the AUTHOR role can write questions");
        }
        return author;
    }

    private Question storeNew(Question question, QuestionMetadata metadata) {
        if (metadata != null) {
            question.attachMetadata(metadata);
        }
        return questions.save(question);
    }
}

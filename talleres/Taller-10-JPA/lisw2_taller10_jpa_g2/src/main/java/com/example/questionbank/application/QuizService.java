package com.example.questionbank.application;

import com.example.questionbank.application.command.CreateQuizCommand;
import com.example.questionbank.domain.exception.BusinessRuleException;
import com.example.questionbank.domain.exception.NotFoundException;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.domain.repository.QuestionRepository;
import com.example.questionbank.domain.repository.QuizRepository;
import com.example.questionbank.domain.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases about quizzes. The domain refuses questions that are not published.
 */
@Service
@Transactional
public class QuizService {

    private final QuizRepository quizzes;
    private final QuestionRepository questions;
    private final UserRepository users;

    /**
     * Creates the service.
     *
     * @param quizzes the quiz repository contract
     * @param questions the question repository contract
     * @param users the user repository contract
     */
    public QuizService(QuizRepository quizzes, QuestionRepository questions, UserRepository users) {
        this.quizzes = quizzes;
        this.questions = questions;
        this.users = users;
    }

    /**
     * Creates a quiz with published questions.
     *
     * @param command the data of the quiz
     * @return the stored quiz
     */
    public Quiz create(CreateQuizCommand command) {
        User owner = users.findById(command.ownerId())
                .orElseThrow(() -> new NotFoundException("Owner not found: " + command.ownerId()));
        List<Long> ids = command.questionIds() == null ? List.of() : command.questionIds();
        if (ids.isEmpty()) {
            throw new BusinessRuleException("A quiz needs at least one question");
        }
        Map<Long, Question> found = new HashMap<>();
        questions.findAllById(ids).forEach(question -> found.put(question.getId(), question));
        Quiz quiz = new Quiz(null, command.title(), owner);
        for (Long id : ids) {
            Question question = found.get(id);
            if (question == null) {
                throw new NotFoundException("Question not found: " + id);
            }
            quiz.addQuestion(question);
        }
        return quizzes.save(quiz);
    }

    /**
     * Finds a quiz.
     *
     * @param id the identity
     * @return the quiz
     */
    @Transactional(readOnly = true)
    public Quiz findById(Long id) {
        return quizzes.findById(id).orElseThrow(() -> new NotFoundException("Quiz not found: " + id));
    }

    /**
     * Lists all quizzes.
     *
     * @return every quiz
     */
    @Transactional(readOnly = true)
    public List<Quiz> list() {
        return quizzes.findAll();
    }
}

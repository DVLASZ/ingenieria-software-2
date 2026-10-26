package com.example.questionbank.infrastructure.jpa;

import com.example.questionbank.domain.exception.NotFoundException;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.repository.QuizRepository;
import com.example.questionbank.infrastructure.jpa.mapper.QuizMapper;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.QuizRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuestionDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringQuizDataRepository;
import com.example.questionbank.infrastructure.jpa.repository.SpringUserDataRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stores quizzes with Spring Data JPA.
 */
@Repository
@Transactional(readOnly = true)
public class QuizRepositoryJpa implements QuizRepository {

    private final SpringQuizDataRepository quizzes;
    private final SpringUserDataRepository users;
    private final SpringQuestionDataRepository questions;

    /**
     * Creates the repository.
     *
     * @param quizzes Spring Data interface for quiz records
     * @param users Spring Data interface for user records
     * @param questions Spring Data interface for question records
     */
    public QuizRepositoryJpa(SpringQuizDataRepository quizzes, SpringUserDataRepository users,
                             SpringQuestionDataRepository questions) {
        this.quizzes = quizzes;
        this.users = users;
        this.questions = questions;
    }

    @Override
    @Transactional
    public Quiz save(Quiz quiz) {
        List<QuestionRecord> questionRecords = resolveQuestions(quiz);
        QuizRecord saved;
        if (quiz.getId() == null) {
            Long ownerId = quiz.getOwner().getId();
            if (ownerId == null) {
                throw new NotFoundException("The owner has not been saved yet");
            }
            UserRecord owner = users.findById(ownerId)
                    .orElseThrow(() -> new NotFoundException("Owner not found: " + ownerId));
            saved = quizzes.save(QuizMapper.toNewRecord(quiz, owner, questionRecords));
        } else {
            QuizRecord record = quizzes.findById(quiz.getId())
                    .orElseThrow(() -> new NotFoundException("Quiz not found: " + quiz.getId()));
            record.getQuestions().clear();
            record.getQuestions().addAll(questionRecords);
            saved = quizzes.save(record);
        }
        return QuizMapper.toDomain(saved);
    }

    @Override
    public Optional<Quiz> findById(Long id) {
        return quizzes.findById(id).map(QuizMapper::toDomain);
    }

    @Override
    public List<Quiz> findAll() {
        return quizzes.findAll(Sort.by("id")).stream().map(QuizMapper::toDomain).toList();
    }

    private List<QuestionRecord> resolveQuestions(Quiz quiz) {
        List<Long> ids = new ArrayList<>();
        for (Question question : quiz.getQuestions()) {
            if (question.getId() == null) {
                throw new NotFoundException("A question of the quiz has not been saved yet");
            }
            ids.add(question.getId());
        }
        Map<Long, QuestionRecord> byId = new HashMap<>();
        questions.findAllById(ids).forEach(record -> byId.put(record.getId(), record));
        List<QuestionRecord> ordered = new ArrayList<>();
        for (Long id : ids) {
            QuestionRecord record = byId.get(id);
            if (record == null) {
                throw new NotFoundException("Question not found: " + id);
            }
            ordered.add(record);
        }
        return ordered;
    }
}

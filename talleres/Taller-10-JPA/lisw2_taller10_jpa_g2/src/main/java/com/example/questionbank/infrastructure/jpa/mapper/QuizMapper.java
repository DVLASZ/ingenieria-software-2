package com.example.questionbank.infrastructure.jpa.mapper;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import com.example.questionbank.infrastructure.jpa.record.QuizRecord;
import com.example.questionbank.infrastructure.jpa.record.UserRecord;
import java.util.List;

/**
 * Converts quizzes between the domain model and the JPA record. It must run inside a transaction.
 */
public final class QuizMapper {

    private QuizMapper() {
    }

    /**
     * Builds the domain quiz from a record, including its questions in order.
     *
     * @param record the JPA record
     * @return the domain quiz
     */
    public static Quiz toDomain(QuizRecord record) {
        List<Question> questions = record.getQuestions().stream().map(QuestionMapper::toDomain).toList();
        return new Quiz(record.getId(), record.getTitle(), UserMapper.toDomain(record.getOwner()), questions);
    }

    /**
     * Builds a new record from a domain quiz.
     *
     * @param quiz the domain quiz
     * @param owner record of the owner, already stored
     * @param questions records of the questions, in the order of the quiz
     * @return a record ready to be inserted
     */
    public static QuizRecord toNewRecord(Quiz quiz, UserRecord owner, List<QuestionRecord> questions) {
        QuizRecord record = new QuizRecord(quiz.getTitle(), owner);
        record.getQuestions().addAll(questions);
        return record;
    }
}

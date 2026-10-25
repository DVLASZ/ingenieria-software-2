package com.example.questionbank.domain;

import static com.example.questionbank.support.TestData.competency;
import static com.example.questionbank.support.TestData.topic;
import static com.example.questionbank.support.TestData.trueFalse;
import static com.example.questionbank.support.TestData.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.questionbank.domain.exception.BusinessRuleException;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.model.Role;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Quiz rules")
class QuizTest {

    private Question published() {
        Question question = trueFalse(user(Role.AUTHOR));
        question.classify(topic(), List.of(competency("CRIT")));
        question.submitForReview();
        question.moveTo(QuestionStatus.IN_REVIEW);
        question.moveTo(QuestionStatus.APPROVED);
        question.moveTo(QuestionStatus.PUBLISHED);
        return question;
    }

    @Test
    void acceptsPublishedQuestions() {
        Quiz quiz = new Quiz(null, "Midterm", user(Role.TEACHER));
        Question question = published();

        quiz.addQuestion(question);

        assertThat(quiz.getQuestions()).containsExactly(question);
    }

    @Test
    void rejectsQuestionsThatAreNotPublished() {
        Quiz quiz = new Quiz(null, "Midterm", user(Role.TEACHER));
        Question draft = trueFalse(user(Role.AUTHOR));

        assertThatThrownBy(() -> quiz.addQuestion(draft)).isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("published");
        assertThat(quiz.getQuestions()).isEmpty();
    }

    @Test
    void rejectsArchivedQuestions() {
        Question question = published();
        question.moveTo(QuestionStatus.ARCHIVED);
        Quiz quiz = new Quiz(null, "Midterm", user(Role.TEACHER));

        assertThatThrownBy(() -> quiz.addQuestion(question)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsTheSameQuestionTwice() {
        Quiz quiz = new Quiz(null, "Midterm", user(Role.TEACHER));
        Question question = published();
        quiz.addQuestion(question);

        assertThatThrownBy(() -> quiz.addQuestion(question)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void onlyTeachersOwnQuizzes() {
        assertThatThrownBy(() -> new Quiz(null, "Midterm", user(Role.STUDENT)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void needsATitle() {
        assertThatThrownBy(() -> new Quiz(null, " ", user(Role.TEACHER))).isInstanceOf(BusinessRuleException.class);
    }
}

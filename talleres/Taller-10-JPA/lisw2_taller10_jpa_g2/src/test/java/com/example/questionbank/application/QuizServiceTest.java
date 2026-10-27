package com.example.questionbank.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.questionbank.application.command.CreateQuizCommand;
import com.example.questionbank.domain.exception.BusinessRuleException;
import com.example.questionbank.domain.exception.NotFoundException;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.support.InMemoryRepositories;
import com.example.questionbank.support.TestData;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Quiz use cases, tested with in-memory repositories")
class QuizServiceTest {

    private InMemoryRepositories.Quizzes quizzes;
    private InMemoryRepositories.Questions questions;
    private InMemoryRepositories.Users users;
    private QuizService service;
    private User author;
    private User teacher;
    private Topic topic;
    private Competency competency;

    @BeforeEach
    void setUp() {
        quizzes = new InMemoryRepositories.Quizzes();
        questions = new InMemoryRepositories.Questions();
        users = new InMemoryRepositories.Users();
        service = new QuizService(quizzes, questions, users);
        author = users.save(TestData.user(Role.AUTHOR));
        teacher = users.save(TestData.user(Role.TEACHER));
        topic = new Topic(1L, "Statistics");
        competency = new Competency(1L, "QUANT", "Quantitative reasoning");
    }

    private Question questionIn(QuestionStatus status) {
        Question question = TestData.trueFalse(author);
        question.classify(topic, List.of(competency));
        if (status != QuestionStatus.DRAFT) {
            question.submitForReview();
            question.moveTo(QuestionStatus.IN_REVIEW);
        }
        if (status == QuestionStatus.APPROVED || status == QuestionStatus.PUBLISHED) {
            question.moveTo(QuestionStatus.APPROVED);
        }
        if (status == QuestionStatus.PUBLISHED) {
            question.moveTo(QuestionStatus.PUBLISHED);
        }
        return questions.save(question);
    }

    @Test
    void createsAQuizWithPublishedQuestionsInOrder() {
        Question first = questionIn(QuestionStatus.PUBLISHED);
        Question second = questionIn(QuestionStatus.PUBLISHED);

        Quiz quiz = service.create(new CreateQuizCommand("Midterm", teacher.getId(),
                List.of(second.getId(), first.getId())));

        assertThat(quiz.getId()).isNotNull();
        assertThat(quiz.getQuestions()).containsExactly(second, first);
        assertThat(service.findById(quiz.getId())).isSameAs(quiz);
        assertThat(service.list()).containsExactly(quiz);
    }

    @Test
    void refusesADraft() {
        Question draft = questionIn(QuestionStatus.DRAFT);

        assertThatThrownBy(() -> service.create(new CreateQuizCommand("Midterm", teacher.getId(),
                List.of(draft.getId())))).isInstanceOf(BusinessRuleException.class).hasMessageContaining("published");
        assertThat(quizzes.findAll()).isEmpty();
    }

    @Test
    void refusesAQuestionThatIsApprovedButNotPublishedYet() {
        Question approved = questionIn(QuestionStatus.APPROVED);

        assertThatThrownBy(() -> service.create(new CreateQuizCommand("Midterm", teacher.getId(),
                List.of(approved.getId())))).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void oneUnpublishedQuestionSpoilsTheWholeQuiz() {
        Question published = questionIn(QuestionStatus.PUBLISHED);
        Question inReview = questionIn(QuestionStatus.IN_REVIEW);

        assertThatThrownBy(() -> service.create(new CreateQuizCommand("Midterm", teacher.getId(),
                List.of(published.getId(), inReview.getId())))).isInstanceOf(BusinessRuleException.class);
        assertThat(quizzes.findAll()).isEmpty();
    }

    @Test
    void refusesTheSameQuestionTwice() {
        Question published = questionIn(QuestionStatus.PUBLISHED);

        assertThatThrownBy(() -> service.create(new CreateQuizCommand("Midterm", teacher.getId(),
                List.of(published.getId(), published.getId())))).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void refusesAQuizWithoutQuestions() {
        assertThatThrownBy(() -> service.create(new CreateQuizCommand("Midterm", teacher.getId(), List.of())))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> service.create(new CreateQuizCommand("Midterm", teacher.getId(), null)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void failsWithAnUnknownQuestionOrOwner() {
        Question published = questionIn(QuestionStatus.PUBLISHED);

        assertThatThrownBy(() -> service.create(new CreateQuizCommand("Midterm", teacher.getId(), List.of(999L))))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.create(new CreateQuizCommand("Midterm", 999L, List.of(published.getId()))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void onlyATeacherCanOwnAQuiz() {
        Question published = questionIn(QuestionStatus.PUBLISHED);

        assertThatThrownBy(() -> service.create(new CreateQuizCommand("Midterm", author.getId(),
                List.of(published.getId())))).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void anUnknownQuizIsNotFound() {
        assertThatThrownBy(() -> service.findById(999L)).isInstanceOf(NotFoundException.class);
    }
}

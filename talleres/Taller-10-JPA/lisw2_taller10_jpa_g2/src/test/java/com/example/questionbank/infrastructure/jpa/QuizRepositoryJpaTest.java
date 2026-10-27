package com.example.questionbank.infrastructure.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.questionbank.domain.exception.NotFoundException;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Quiz;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.User;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Quiz repository with JPA and H2")
class QuizRepositoryJpaTest extends PersistenceTestSupport {

    private final User[] people = new User[2];
    private Topic topic;
    private Competency competency;

    private void prepare() {
        people[0] = saveUser(Role.AUTHOR, "ada");
        people[1] = saveUser(Role.TEACHER, "grace");
        topic = saveTopic("Statistics");
        competency = saveCompetency("QUANT");
    }

    @Test
    void savesAQuizWithItsQuestionsInOrder() {
        prepare();
        Question first = publishedQuestion(people[0], topic, competency, "First");
        Question second = publishedQuestion(people[0], topic, competency, "Second");
        Quiz quiz = new Quiz(null, "Midterm", people[1]);
        quiz.addQuestion(second);
        quiz.addQuestion(first);

        Quiz saved = quizzes.save(quiz);
        flushAndClear();
        Quiz found = quizzes.findById(saved.getId()).orElseThrow();

        assertThat(found.getTitle()).isEqualTo("Midterm");
        assertThat(found.getOwner().getUsername()).isEqualTo("grace");
        assertThat(found.getQuestions()).extracting(Question::getId).containsExactly(second.getId(), first.getId());
        assertThat(jdbc.queryForList("select position from quiz_questions where quiz_id = ? order by position",
                Integer.class, saved.getId())).containsExactly(0, 1);
    }

    @Test
    void addsAQuestionToAnExistingQuiz() {
        prepare();
        Question first = publishedQuestion(people[0], topic, competency, "First");
        Question second = publishedQuestion(people[0], topic, competency, "Second");
        Quiz quiz = new Quiz(null, "Midterm", people[1]);
        quiz.addQuestion(first);
        Quiz saved = quizzes.save(quiz);
        flushAndClear();

        Quiz loaded = quizzes.findById(saved.getId()).orElseThrow();
        loaded.addQuestion(second);
        quizzes.save(loaded);
        flushAndClear();

        assertThat(quizzes.findById(saved.getId()).orElseThrow().getQuestions()).hasSize(2);
        assertThat(countRows("quiz_questions")).isEqualTo(2);
    }

    @Test
    void aQuizKeepsAQuestionThatWasArchivedLater() {
        prepare();
        Question question = publishedQuestion(people[0], topic, competency, "First");
        Quiz quiz = new Quiz(null, "Midterm", people[1]);
        quiz.addQuestion(question);
        Quiz saved = quizzes.save(quiz);
        question.moveTo(QuestionStatus.ARCHIVED);
        questions.save(question);
        flushAndClear();

        Quiz found = quizzes.findById(saved.getId()).orElseThrow();

        assertThat(found.getQuestions()).hasSize(1);
        assertThat(found.getQuestions().get(0).getStatus()).isEqualTo(QuestionStatus.ARCHIVED);
    }

    @Test
    void listsAllTheQuizzes() {
        prepare();
        quizzes.save(new Quiz(null, "One", people[1]));
        quizzes.save(new Quiz(null, "Two", people[1]));
        flushAndClear();

        assertThat(quizzes.findAll()).extracting(Quiz::getTitle).containsExactlyInAnyOrder("One", "Two");
    }

    @Test
    void rejectsAnOwnerThatDoesNotExist() {
        User ghost = new User(9999L, "ghost", "Ghost", "ghost@example.com", Role.TEACHER);

        assertThatThrownBy(() -> quizzes.save(new Quiz(null, "Midterm", ghost)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void rejectsAQuestionThatWasNeverSaved() {
        prepare();
        Question unsaved = newTrueFalse(people[0], "Never saved");
        unsaved.classify(topic, List.of(competency));
        unsaved.submitForReview();
        unsaved.moveTo(QuestionStatus.IN_REVIEW);
        unsaved.moveTo(QuestionStatus.APPROVED);
        unsaved.moveTo(QuestionStatus.PUBLISHED);
        Quiz quiz = new Quiz(null, "Midterm", people[1]);
        quiz.addQuestion(unsaved);

        assertThatThrownBy(() -> quizzes.save(quiz)).isInstanceOf(NotFoundException.class);
    }
}

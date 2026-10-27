package com.example.questionbank.infrastructure.jpa;

import static com.example.questionbank.support.TestData.multipleChoice;
import static com.example.questionbank.support.TestData.validOptions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.questionbank.domain.exception.NotFoundException;
import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Question repository with JPA and H2")
class QuestionRepositoryJpaTest extends PersistenceTestSupport {

    @Test
    void savesAndReadsAMultipleChoiceQuestionWithItsOptionsInOrder() {
        User author = saveUser(Role.AUTHOR, "ada");

        Question saved = questions.save(multipleChoice(author));
        flushAndClear();
        Question found = questions.findById(saved.getId()).orElseThrow();

        assertThat(found).isInstanceOf(MultipleChoiceQuestion.class);
        MultipleChoiceQuestion question = (MultipleChoiceQuestion) found;
        assertThat(question.getOptions()).containsExactlyElementsOf(validOptions());
        assertThat(question.getCorrectOption().text()).isEqualTo("The author defends a thesis");
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.DRAFT);
        assertThat(question.getAuthor().getUsername()).isEqualTo("ada");
    }

    @Test
    void savesAndReadsATrueFalseQuestion() {
        User author = saveUser(Role.AUTHOR, "ada");
        TrueFalseQuestion draft = new TrueFalseQuestion(null, "The earth is flat.",
                com.example.questionbank.domain.model.Difficulty.HARD, author, false);

        Question saved = questions.save(draft);
        flushAndClear();
        Question found = questions.findById(saved.getId()).orElseThrow();

        assertThat(found).isInstanceOf(TrueFalseQuestion.class);
        assertThat(((TrueFalseQuestion) found).isExpectedAnswer()).isFalse();
        assertThat(found.getStatement()).isEqualTo("The earth is flat.");
    }

    @Test
    void keepsTheAuthorTheTopicTheCompetenciesAndTheMetadata() {
        User author = saveUser(Role.AUTHOR, "ada");
        Topic topic = saveTopic("Argumentative texts");
        Competency critical = saveCompetency("CRIT");
        Competency writing = saveCompetency("WRIT");
        Question saved = questions.save(newTrueFalse(author, "Statement"));
        saved.classify(topic, List.of(critical, writing));
        saved.attachMetadata(new QuestionMetadata("Analysis", 90, "Check the wording", LocalDate.of(2026, 10, 1)));
        questions.save(saved);
        flushAndClear();

        Question found = questions.findById(saved.getId()).orElseThrow();

        assertThat(found.getTopic().getName()).isEqualTo("Argumentative texts");
        assertThat(found.getCompetencies().stream().map(Competency::getCode).collect(Collectors.toSet()))
                .containsExactlyInAnyOrder("CRIT", "WRIT");
        assertThat(found.getMetadata().estimatedSeconds()).isEqualTo(90);
        assertThat(found.getMetadata().lastReviewedAt()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(countRows("question_competencies")).isEqualTo(2);
        assertThat(countRows("question_metadata")).isEqualTo(1);
    }

    @Test
    void findAllReturnsBothSubtypes() {
        User author = saveUser(Role.AUTHOR, "ada");
        questions.save(multipleChoice(author));
        questions.save(newTrueFalse(author, "Statement"));
        flushAndClear();

        List<Question> all = questions.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).hasAtLeastOneElementOfType(MultipleChoiceQuestion.class)
                .hasAtLeastOneElementOfType(TrueFalseQuestion.class);
    }

    @Test
    void storesTheHierarchyInJoinedTables() {
        User author = saveUser(Role.AUTHOR, "ada");
        questions.save(multipleChoice(author));
        questions.save(newTrueFalse(author, "Statement"));
        flushAndClear();

        assertThat(countRows("questions")).isEqualTo(2);
        assertThat(countRows("multiple_choice_questions")).isEqualTo(1);
        assertThat(countRows("true_false_questions")).isEqualTo(1);
        assertThat(countRows("answer_options")).isEqualTo(5);
    }

    @Test
    void filtersByAuthorByStatusAndByCompetencyOfPublishedQuestions() {
        User ada = saveUser(Role.AUTHOR, "ada");
        User alan = saveUser(Role.AUTHOR, "alan");
        Topic topic = saveTopic("Statistics");
        Competency critical = saveCompetency("CRIT");
        Competency quantitative = saveCompetency("QUANT");
        Question published = publishedQuestion(ada, topic, critical, "Published and critical");
        publishedQuestion(alan, topic, quantitative, "Published and quantitative");
        Question draft = questions.save(newTrueFalse(ada, "Still a draft"));
        draft.classify(topic, List.of(critical));
        questions.save(draft);
        flushAndClear();

        assertThat(questions.findByAuthorId(ada.getId())).hasSize(2);
        assertThat(questions.findByAuthorId(alan.getId())).hasSize(1);
        assertThat(questions.findByStatus(QuestionStatus.PUBLISHED)).hasSize(2);
        assertThat(questions.findByStatus(QuestionStatus.DRAFT)).hasSize(1);
        assertThat(questions.findPublishedByCompetencyId(critical.getId()))
                .extracting(Question::getId).containsExactly(published.getId());
    }

    @Test
    void persistsTheStatusChangesOfTheLifeCycle() {
        User author = saveUser(Role.AUTHOR, "ada");
        Question published = publishedQuestion(author, saveTopic("Topic"), saveCompetency("CRIT"), "Statement");
        flushAndClear();

        assertThat(questions.findById(published.getId()).orElseThrow().getStatus())
                .isEqualTo(QuestionStatus.PUBLISHED);
    }

    @Test
    void replacingTheOptionsDeletesTheOldRows() {
        User author = saveUser(Role.AUTHOR, "ada");
        MultipleChoiceQuestion saved = (MultipleChoiceQuestion) questions.save(multipleChoice(author));
        flushAndClear();
        MultipleChoiceQuestion loaded = (MultipleChoiceQuestion) questions.findById(saved.getId()).orElseThrow();

        loaded.replaceOptions(List.of(
                new AnswerOption("New correct", true), new AnswerOption("New 1", false),
                new AnswerOption("New 2", false), new AnswerOption("New 3", false),
                new AnswerOption("New 4", false)));
        questions.save(loaded);
        flushAndClear();

        MultipleChoiceQuestion reloaded = (MultipleChoiceQuestion) questions.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getOptions()).extracting(AnswerOption::text)
                .containsExactly("New correct", "New 1", "New 2", "New 3", "New 4");
        assertThat(countRows("answer_options")).isEqualTo(5);
    }

    @Test
    void updatesTheMetadataInPlaceWithoutAddingRows() {
        User author = saveUser(Role.AUTHOR, "ada");
        Question saved = questions.save(newTrueFalse(author, "Statement"));
        saved.attachMetadata(new QuestionMetadata("Analysis", 60, null, null));
        questions.save(saved);
        flushAndClear();
        assertThat(countRows("question_metadata")).isEqualTo(1);

        saved.attachMetadata(new QuestionMetadata("Synthesis", 120, null, null));
        questions.save(saved);
        flushAndClear();

        assertThat(countRows("question_metadata")).isEqualTo(1);
        assertThat(questions.findById(saved.getId()).orElseThrow().getMetadata().cognitiveLevel())
                .isEqualTo("Synthesis");
    }

    @Test
    void rejectsAnAuthorThatDoesNotExist() {
        User ghost = new User(9999L, "ghost", "Ghost", "ghost@example.com", Role.AUTHOR);

        assertThatThrownBy(() -> questions.save(newTrueFalse(ghost, "Statement")))
                .isInstanceOf(NotFoundException.class).hasMessageContaining("Author");
    }

    @Test
    void rejectsAnAuthorThatWasNeverSaved() {
        User unsaved = new User(null, "unsaved", "Unsaved", "unsaved@example.com", Role.AUTHOR);

        assertThatThrownBy(() -> questions.save(newTrueFalse(unsaved, "Statement")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void rejectsACompetencyThatDoesNotExist() {
        User author = saveUser(Role.AUTHOR, "ada");
        Topic topic = saveTopic("Topic");
        Question saved = questions.save(newTrueFalse(author, "Statement"));
        saved.classify(topic, List.of(new Competency(9999L, "GHOST", "Ghost competency")));

        assertThatThrownBy(() -> questions.save(saved)).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("competencies");
    }

    @Test
    void rejectsATopicThatDoesNotExist() {
        User author = saveUser(Role.AUTHOR, "ada");
        Competency competency = saveCompetency("CRIT");
        Question saved = questions.save(newTrueFalse(author, "Statement"));
        saved.classify(new Topic(9999L, "Ghost topic"), List.of(competency));

        assertThatThrownBy(() -> questions.save(saved)).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Topic");
    }

    @Test
    void anUnknownQuestionIsNotFound() {
        assertThat(questions.findById(12345L)).isEmpty();
    }

    @Test
    void reclassifyingADraftReplacesTheTopicAndTheCompetencyRows() {
        User author = saveUser(Role.AUTHOR, "ada");
        Topic first = saveTopic("First topic");
        Topic second = saveTopic("Second topic");
        Competency critical = saveCompetency("CRIT");
        Competency quantitative = saveCompetency("QUANT");
        Question saved = questions.save(newTrueFalse(author, "Statement"));
        saved.classify(first, List.of(critical));
        questions.save(saved);
        flushAndClear();

        Question loaded = questions.findById(saved.getId()).orElseThrow();
        loaded.classify(second, List.of(quantitative));
        questions.save(loaded);
        flushAndClear();

        Question reloaded = questions.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getTopic().getName()).isEqualTo("Second topic");
        assertThat(reloaded.getCompetencies()).extracting(Competency::getCode).containsExactly("QUANT");
        assertThat(countRows("question_competencies")).isEqualTo(1);
    }

    @Test
    void aRejectedQuestionGoesBackToDraftAndTheChangeIsStored() {
        User author = saveUser(Role.AUTHOR, "ada");
        Question saved = questions.save(newTrueFalse(author, "Statement"));
        saved.classify(saveTopic("Topic"), List.of(saveCompetency("CRIT")));
        saved.submitForReview();
        saved.moveTo(QuestionStatus.IN_REVIEW);
        saved.moveTo(QuestionStatus.REJECTED);
        questions.save(saved);
        flushAndClear();
        assertThat(questions.findById(saved.getId()).orElseThrow().getStatus()).isEqualTo(QuestionStatus.REJECTED);

        Question loaded = questions.findById(saved.getId()).orElseThrow();
        loaded.moveTo(QuestionStatus.DRAFT);
        loaded.updateStatement("Corrected statement");
        questions.save(loaded);
        flushAndClear();

        Question reloaded = questions.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(QuestionStatus.DRAFT);
        assertThat(reloaded.getStatement()).isEqualTo("Corrected statement");
    }

    @Test
    void savingUnchangedOptionsDoesNotRewriteTheirRows() {
        User author = saveUser(Role.AUTHOR, "ada");
        MultipleChoiceQuestion saved = (MultipleChoiceQuestion) questions.save(multipleChoice(author));
        flushAndClear();
        List<Long> before = jdbc.queryForList("select id from answer_options order by id", Long.class);

        MultipleChoiceQuestion loaded = (MultipleChoiceQuestion) questions.findById(saved.getId()).orElseThrow();
        loaded.updateStatement("Same options, new statement");
        questions.save(loaded);
        flushAndClear();

        assertThat(jdbc.queryForList("select id from answer_options order by id", Long.class)).isEqualTo(before);
    }

    @Test
    void listsComeBackOrderedById() {
        User author = saveUser(Role.AUTHOR, "ada");
        Question first = questions.save(newTrueFalse(author, "First"));
        Question second = questions.save(newTrueFalse(author, "Second"));
        Question third = questions.save(newTrueFalse(author, "Third"));
        flushAndClear();

        assertThat(questions.findAll()).extracting(Question::getId)
                .containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(questions.findByAuthorId(author.getId())).extracting(Question::getId)
                .containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(questions.findByStatus(QuestionStatus.DRAFT)).extracting(Question::getId)
                .containsExactly(first.getId(), second.getId(), third.getId());
    }
}

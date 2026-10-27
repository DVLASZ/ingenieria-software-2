package com.example.questionbank.application;

import static com.example.questionbank.support.TestData.validOptions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.questionbank.application.command.CreateMultipleChoiceQuestionCommand;
import com.example.questionbank.application.command.CreateTrueFalseQuestionCommand;
import com.example.questionbank.domain.exception.BusinessRuleException;
import com.example.questionbank.domain.exception.NotFoundException;
import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.Topic;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import com.example.questionbank.domain.model.User;
import com.example.questionbank.support.InMemoryRepositories;
import com.example.questionbank.support.TestData;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Question use cases, tested with in-memory repositories")
class QuestionServiceTest {

    private InMemoryRepositories.Questions questions;
    private InMemoryRepositories.Users users;
    private InMemoryRepositories.Topics topics;
    private InMemoryRepositories.Competencies competencies;
    private QuestionService service;
    private User author;
    private Topic topic;
    private Competency competency;

    @BeforeEach
    void setUp() {
        questions = new InMemoryRepositories.Questions();
        users = new InMemoryRepositories.Users();
        topics = new InMemoryRepositories.Topics();
        competencies = new InMemoryRepositories.Competencies();
        service = new QuestionService(questions, users, topics, competencies);
        author = users.save(TestData.user(Role.AUTHOR));
        topic = topics.save(TestData.topic());
        competency = competencies.save(TestData.competency("CRIT"));
    }

    private Question trueFalse() {
        return service.createTrueFalse(
                new CreateTrueFalseQuestionCommand("The text defends a thesis.", Difficulty.EASY, author.getId(),
                        true, null));
    }

    private Question classifiedTrueFalse() {
        Question question = trueFalse();
        return service.classify(question.getId(), topic.getId(), List.of(competency.getId()));
    }

    @Test
    void createsAMultipleChoiceDraftWithMetadata() {
        QuestionMetadata metadata = new QuestionMetadata("Analysis", 90, null, null);

        Question created = service.createMultipleChoice(new CreateMultipleChoiceQuestionCommand(
                "What is the main idea?", Difficulty.MEDIUM, author.getId(), validOptions(), metadata));

        assertThat(created.getId()).isNotNull();
        assertThat(created).isInstanceOf(MultipleChoiceQuestion.class);
        assertThat(created.getStatus()).isEqualTo(QuestionStatus.DRAFT);
        assertThat(created.getMetadata()).isEqualTo(metadata);
    }

    @Test
    void createsATrueFalseDraft() {
        Question created = trueFalse();

        assertThat(created).isInstanceOf(TrueFalseQuestion.class);
        assertThat(service.findById(created.getId())).isSameAs(created);
    }

    @Test
    void rejectsAnAuthorThatDoesNotExist() {
        assertThatThrownBy(() -> service.createTrueFalse(
                new CreateTrueFalseQuestionCommand("Statement", Difficulty.EASY, 999L, true, null)))
                .isInstanceOf(NotFoundException.class);
        assertThat(questions.findAll()).isEmpty();
    }

    @Test
    void rejectsAUserThatIsNotAnAuthor() {
        User student = users.save(new User(null, "student1", "Student", "student1@example.com", Role.STUDENT));

        assertThatThrownBy(() -> service.createTrueFalse(
                new CreateTrueFalseQuestionCommand("Statement", Difficulty.EASY, student.getId(), true, null)))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(questions.findAll()).isEmpty();
    }

    @Test
    void classifiesAQuestionWithATopicAndCompetencies() {
        Competency writing = competencies.save(TestData.competency("WRIT"));
        Question question = trueFalse();

        Question classified = service.classify(question.getId(), topic.getId(),
                List.of(competency.getId(), writing.getId()));

        assertThat(classified.getTopic()).isEqualTo(topic);
        assertThat(classified.getCompetencies()).containsExactlyInAnyOrder(competency, writing);
    }

    @Test
    void classifyFailsWithAnUnknownTopicOrCompetency() {
        Question question = trueFalse();

        assertThatThrownBy(() -> service.classify(question.getId(), 999L, List.of(competency.getId())))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.classify(question.getId(), topic.getId(), List.of(competency.getId(), 999L)))
                .isInstanceOf(NotFoundException.class);
        assertThat(question.getTopic()).isNull();
    }

    @Test
    void submitsAClassifiedDraftForReview() {
        Question question = classifiedTrueFalse();

        Question submitted = service.submitForReview(question.getId());

        assertThat(submitted.getStatus()).isEqualTo(QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void submitFailsWithoutClassificationAndNothingChanges() {
        Question question = trueFalse();

        assertThatThrownBy(() -> service.submitForReview(question.getId())).isInstanceOf(BusinessRuleException.class);
        assertThat(questions.findById(question.getId()).orElseThrow().getStatus()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    void submitFailsForAMultipleChoiceWithTwoCorrectOptions() {
        List<AnswerOption> options = List.of(new AnswerOption("A", true), new AnswerOption("B", true),
                new AnswerOption("C", false), new AnswerOption("D", false), new AnswerOption("E", false));
        Question question = service.createMultipleChoice(new CreateMultipleChoiceQuestionCommand(
                "Statement", Difficulty.HARD, author.getId(), options, null));
        service.classify(question.getId(), topic.getId(), List.of(competency.getId()));

        assertThatThrownBy(() -> service.submitForReview(question.getId())).isInstanceOf(BusinessRuleException.class);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    void changesTheStatusAlongTheLifeCycleAndRefusesJumps() {
        Question question = classifiedTrueFalse();
        service.submitForReview(question.getId());

        assertThatThrownBy(() -> service.changeStatus(question.getId(), QuestionStatus.PUBLISHED))
                .isInstanceOf(BusinessRuleException.class);
        service.changeStatus(question.getId(), QuestionStatus.IN_REVIEW);
        service.changeStatus(question.getId(), QuestionStatus.APPROVED);
        Question published = service.changeStatus(question.getId(), QuestionStatus.PUBLISHED);

        assertThat(published.isPublished()).isTrue();
    }

    @Test
    void listsByAuthorByStatusByBothOrWithoutFilter() {
        User other = users.save(new User(null, "other", "Other", "other@example.com", Role.AUTHOR));
        Question mine = classifiedTrueFalse();
        service.submitForReview(mine.getId());
        service.createTrueFalse(new CreateTrueFalseQuestionCommand("Another", Difficulty.EASY, author.getId(), false,
                null));
        service.createTrueFalse(new CreateTrueFalseQuestionCommand("Not mine", Difficulty.EASY, other.getId(), false,
                null));

        assertThat(service.list(null, null)).hasSize(3);
        assertThat(service.list(author.getId(), null)).hasSize(2);
        assertThat(service.list(null, QuestionStatus.DRAFT)).hasSize(2);
        assertThat(service.list(author.getId(), QuestionStatus.PENDING_REVIEW)).containsExactly(mine);
    }

    @Test
    void listsOnlyPublishedQuestionsOfACompetency() {
        Question published = classifiedTrueFalse();
        service.submitForReview(published.getId());
        service.changeStatus(published.getId(), QuestionStatus.IN_REVIEW);
        service.changeStatus(published.getId(), QuestionStatus.APPROVED);
        service.changeStatus(published.getId(), QuestionStatus.PUBLISHED);
        Question draft = trueFalse();
        service.classify(draft.getId(), topic.getId(), List.of(competency.getId()));

        assertThat(service.listPublishedByCompetency(competency.getId())).containsExactly(published);
        assertThatThrownBy(() -> service.listPublishedByCompetency(999L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void anUnknownQuestionIsNotFound() {
        assertThatThrownBy(() -> service.findById(999L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void classifyFailsOnceTheQuestionLeftDraft() {
        Question question = classifiedTrueFalse();
        service.submitForReview(question.getId());

        assertThatThrownBy(() -> service.classify(question.getId(), topic.getId(), List.of(competency.getId())))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void changeStatusNeedsATargetStatus() {
        Question question = trueFalse();

        assertThatThrownBy(() -> service.changeStatus(question.getId(), null))
                .isInstanceOf(BusinessRuleException.class);
    }
}

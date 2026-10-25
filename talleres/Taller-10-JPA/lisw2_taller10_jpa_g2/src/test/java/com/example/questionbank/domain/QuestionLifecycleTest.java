package com.example.questionbank.domain;

import static com.example.questionbank.support.TestData.competency;
import static com.example.questionbank.support.TestData.topic;
import static com.example.questionbank.support.TestData.trueFalse;
import static com.example.questionbank.support.TestData.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.questionbank.domain.exception.BusinessRuleException;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.Role;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Question lifecycle rules")
class QuestionLifecycleTest {

    private TrueFalseQuestion question;

    @BeforeEach
    void setUp() {
        question = trueFalse(user(Role.AUTHOR));
    }

    private void classify() {
        question.classify(topic(), List.of(competency("CRIT")));
    }

    @Test
    void startsAsDraft() {
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.DRAFT);
        assertThat(question.getTopic()).isNull();
        assertThat(question.getCompetencies()).isEmpty();
    }

    @Test
    void rejectsABlankStatement() {
        assertThatThrownBy(() -> new TrueFalseQuestion(null, " ", question.getDifficulty(), question.getAuthor(), true))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsAMissingAuthor() {
        assertThatThrownBy(() -> new TrueFalseQuestion(null, "Statement", question.getDifficulty(), null, true))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void classifyStoresTopicAndCompetencies() {
        classify();

        assertThat(question.getTopic().getName()).isEqualTo("Argumentative texts");
        assertThat(question.getCompetencies()).hasSize(1);
    }

    @Test
    void classifyNeedsATopicAndAtLeastOneCompetency() {
        assertThatThrownBy(() -> question.classify(null, List.of(competency("CRIT"))))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> question.classify(topic(), List.of())).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void submitForReviewMovesToPendingReview() {
        classify();

        question.submitForReview();

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void submitWithoutTopicFailsAndKeepsTheStatus() {
        assertThatThrownBy(question::submitForReview).isInstanceOf(BusinessRuleException.class);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    void submitTwiceFails() {
        classify();
        question.submitForReview();

        assertThatThrownBy(question::submitForReview).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void classifyIsOnlyAllowedWhileDraft() {
        classify();
        question.submitForReview();

        assertThatThrownBy(this::classify).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void movesThroughTheWholeLifecycle() {
        classify();
        question.submitForReview();
        question.moveTo(QuestionStatus.IN_REVIEW);
        question.moveTo(QuestionStatus.APPROVED);
        question.moveTo(QuestionStatus.PUBLISHED);

        assertThat(question.isPublished()).isTrue();
        question.moveTo(QuestionStatus.ARCHIVED);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ARCHIVED);
    }

    @Test
    void rejectedQuestionGoesBackToDraftAndCanBeEdited() {
        classify();
        question.submitForReview();
        question.moveTo(QuestionStatus.IN_REVIEW);
        question.moveTo(QuestionStatus.REJECTED);
        question.moveTo(QuestionStatus.DRAFT);

        question.updateStatement("A better statement.");

        assertThat(question.getStatement()).isEqualTo("A better statement.");
    }

    @Test
    void cannotEditTheStatementOutsideDraft() {
        classify();
        question.submitForReview();

        assertThatThrownBy(() -> question.updateStatement("Late edit")).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void invalidTransitionFailsAndKeepsTheStatus() {
        assertThatThrownBy(() -> question.moveTo(QuestionStatus.PUBLISHED)).isInstanceOf(BusinessRuleException.class);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    void movingToPendingReviewAppliesTheSubmissionRules() {
        assertThatThrownBy(() -> question.moveTo(QuestionStatus.PENDING_REVIEW))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void metadataMustHaveAPositiveEstimatedTime() {
        assertThatThrownBy(() -> new QuestionMetadata("Analysis", 0, "notes", null))
                .isInstanceOf(BusinessRuleException.class);
        question.attachMetadata(new QuestionMetadata("Analysis", 90, "notes", LocalDate.of(2026, 10, 1)));
        assertThat(question.getMetadata().estimatedSeconds()).isEqualTo(90);
    }
}

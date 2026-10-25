package com.example.questionbank.domain;

import static com.example.questionbank.domain.model.QuestionStatus.APPROVED;
import static com.example.questionbank.domain.model.QuestionStatus.ARCHIVED;
import static com.example.questionbank.domain.model.QuestionStatus.DRAFT;
import static com.example.questionbank.domain.model.QuestionStatus.IN_REVIEW;
import static com.example.questionbank.domain.model.QuestionStatus.PENDING_REVIEW;
import static com.example.questionbank.domain.model.QuestionStatus.PUBLISHED;
import static com.example.questionbank.domain.model.QuestionStatus.REJECTED;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.questionbank.domain.model.QuestionStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("Question status transitions")
class QuestionStatusTest {

    @ParameterizedTest(name = "{0} can move to {1}")
    @CsvSource({
            "DRAFT, PENDING_REVIEW",
            "PENDING_REVIEW, IN_REVIEW",
            "IN_REVIEW, APPROVED",
            "IN_REVIEW, REJECTED",
            "REJECTED, DRAFT",
            "APPROVED, PUBLISHED",
            "PUBLISHED, ARCHIVED"
    })
    void allowsTheLifecycleTransitions(QuestionStatus from, QuestionStatus to) {
        assertThat(from.canMoveTo(to)).isTrue();
    }

    @ParameterizedTest(name = "{0} cannot move to {1}")
    @CsvSource({
            "DRAFT, APPROVED",
            "DRAFT, PUBLISHED",
            "PENDING_REVIEW, APPROVED",
            "IN_REVIEW, PUBLISHED",
            "APPROVED, DRAFT",
            "REJECTED, PUBLISHED",
            "PUBLISHED, DRAFT",
            "DRAFT, DRAFT"
    })
    void rejectsTransitionsThatSkipOrGoBack(QuestionStatus from, QuestionStatus to) {
        assertThat(from.canMoveTo(to)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = QuestionStatus.class, names = "ARCHIVED", mode = EnumSource.Mode.EXCLUDE)
    void anyStatusCanBeArchivedExceptArchived(QuestionStatus from) {
        assertThat(from.canMoveTo(ARCHIVED)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(QuestionStatus.class)
    void archivedIsTerminal(QuestionStatus target) {
        assertThat(ARCHIVED.canMoveTo(target)).isFalse();
    }

    @Test
    void onlyPublishedQuestionsAreUsable() {
        assertThat(PUBLISHED.isPublished()).isTrue();
        assertThat(List.of(DRAFT, PENDING_REVIEW, IN_REVIEW, APPROVED, REJECTED, ARCHIVED))
                .noneMatch(QuestionStatus::isPublished);
    }
}

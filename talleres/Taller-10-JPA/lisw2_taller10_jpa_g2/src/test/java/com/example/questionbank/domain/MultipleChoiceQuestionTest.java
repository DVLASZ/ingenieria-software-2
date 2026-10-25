package com.example.questionbank.domain;

import static com.example.questionbank.support.TestData.competency;
import static com.example.questionbank.support.TestData.multipleChoice;
import static com.example.questionbank.support.TestData.topic;
import static com.example.questionbank.support.TestData.user;
import static com.example.questionbank.support.TestData.validOptions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.questionbank.domain.exception.BusinessRuleException;
import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.QuestionType;
import com.example.questionbank.domain.model.Role;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Multiple choice question")
class MultipleChoiceQuestionTest {

    private MultipleChoiceQuestion classified(List<AnswerOption> options) {
        MultipleChoiceQuestion question =
                new MultipleChoiceQuestion(null, "Statement", Difficulty.HARD, user(Role.AUTHOR), options);
        question.classify(topic(), List.of(competency("CRIT")));
        return question;
    }

    @Test
    void hasTheMultipleChoiceType() {
        assertThat(multipleChoice(user(Role.AUTHOR)).getType()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
    }

    @Test
    void acceptsOneCorrectOptionAndFourDistractors() {
        MultipleChoiceQuestion question = classified(validOptions());

        question.submitForReview();

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.PENDING_REVIEW);
        assertThat(question.getOptions()).hasSize(5);
    }

    @Test
    void exposesTheCorrectOption() {
        assertThat(multipleChoice(user(Role.AUTHOR)).getCorrectOption().text())
                .isEqualTo("The author defends a thesis");
    }

    @Test
    void rejectsTwoCorrectOptions() {
        List<AnswerOption> options = List.of(
                new AnswerOption("A", true), new AnswerOption("B", true), new AnswerOption("C", false),
                new AnswerOption("D", false), new AnswerOption("E", false));

        assertThatThrownBy(() -> classified(options).submitForReview()).isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("correct");
    }

    @Test
    void rejectsFewerThanFourDistractors() {
        List<AnswerOption> options = List.of(
                new AnswerOption("A", true), new AnswerOption("B", false), new AnswerOption("C", false),
                new AnswerOption("D", false));

        assertThatThrownBy(() -> classified(options).submitForReview()).isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("distractors");
    }

    @Test
    void rejectsRepeatedOptionTexts() {
        List<AnswerOption> options = List.of(
                new AnswerOption("A", true), new AnswerOption("A", false), new AnswerOption("C", false),
                new AnswerOption("D", false), new AnswerOption("E", false));

        assertThatThrownBy(() -> classified(options).submitForReview()).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void failedValidationKeepsTheDraftStatus() {
        MultipleChoiceQuestion question = classified(List.of(new AnswerOption("A", true)));

        assertThatThrownBy(question::submitForReview).isInstanceOf(BusinessRuleException.class);
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    void optionsCanBeReplacedWhileDraftOnly() {
        MultipleChoiceQuestion question = classified(validOptions());
        question.replaceOptions(validOptions());
        question.submitForReview();

        assertThatThrownBy(() -> question.replaceOptions(validOptions())).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void optionListIsNotModifiableFromOutside() {
        MultipleChoiceQuestion question = multipleChoice(user(Role.AUTHOR));

        assertThatThrownBy(() -> question.getOptions().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void anOptionNeedsText() {
        assertThatThrownBy(() -> new AnswerOption(" ", false)).isInstanceOf(BusinessRuleException.class);
    }
}

package com.example.questionbank.api.dto;

import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.Competency;
import com.example.questionbank.domain.model.Difficulty;
import com.example.questionbank.domain.model.MultipleChoiceQuestion;
import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.QuestionMetadata;
import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.domain.model.QuestionType;
import com.example.questionbank.domain.model.TrueFalseQuestion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Request and response objects for questions.
 */
public final class QuestionDtos {

    private QuestionDtos() {
    }

    /**
     * An answer option in a request or a response.
     *
     * @param text what the option says
     * @param correct whether it is the right answer
     */
    public record OptionDto(@NotBlank @Size(max = 500) String text, boolean correct) {
    }

    /**
     * Editorial data in a request or a response.
     *
     * @param cognitiveLevel cognitive level
     * @param estimatedSeconds estimated answering time in seconds
     * @param editorialNotes editorial notes, may be null
     * @param lastReviewedAt day of the last review, may be null
     */
    public record MetadataDto(@NotBlank @Size(max = 60) String cognitiveLevel, @Positive int estimatedSeconds,
                              @Size(max = 1000) String editorialNotes, LocalDate lastReviewedAt) {

        /**
         * Converts the DTO into the domain value.
         *
         * @return the domain metadata
         */
        public QuestionMetadata toDomain() {
            return new QuestionMetadata(cognitiveLevel, estimatedSeconds, editorialNotes, lastReviewedAt);
        }
    }

    /**
     * Body to create a multiple choice question.
     *
     * @param statement what the question asks
     * @param difficulty how hard it is
     * @param authorId identity of the author
     * @param options the options
     * @param metadata editorial data, optional
     */
    public record CreateMultipleChoiceRequest(@NotBlank @Size(max = 2000) String statement, @NotNull Difficulty difficulty,
                                              @NotNull Long authorId,
                                              @NotEmpty List<@NotNull @Valid OptionDto> options,
                                              @Valid MetadataDto metadata) {
    }

    /**
     * Body to create a true/false question.
     *
     * @param statement the statement to judge
     * @param difficulty how hard it is
     * @param authorId identity of the author
     * @param expectedAnswer the right answer
     * @param metadata editorial data, optional
     */
    public record CreateTrueFalseRequest(@NotBlank @Size(max = 2000) String statement, @NotNull Difficulty difficulty,
                                         @NotNull Long authorId, @NotNull Boolean expectedAnswer,
                                         @Valid MetadataDto metadata) {
    }

    /**
     * Body to classify a question.
     *
     * @param topicId identity of the topic
     * @param competencyIds identities of the competencies
     */
    public record ClassifyRequest(@NotNull Long topicId, @NotEmpty Set<@NotNull Long> competencyIds) {
    }

    /**
     * Body to change the status of a question.
     *
     * @param target the new status
     */
    public record ChangeStatusRequest(@NotNull QuestionStatus target) {
    }

    /**
     * A question as the API shows it. Fields that do not apply to the type are null.
     *
     * @param id identity
     * @param type kind of question
     * @param statement what the question asks
     * @param difficulty how hard it is
     * @param status current status
     * @param authorId identity of the author
     * @param topicId identity of the topic, or null
     * @param competencyIds identities of the competencies
     * @param options options, only for multiple choice
     * @param expectedAnswer right answer, only for true/false
     * @param metadata editorial data, or null
     */
    public record QuestionResponse(Long id, QuestionType type, String statement, Difficulty difficulty,
                                   QuestionStatus status, Long authorId, Long topicId, List<Long> competencyIds,
                                   List<OptionDto> options, Boolean expectedAnswer, MetadataDto metadata) {

        /**
         * Builds the response from a domain question of any type.
         *
         * @param question the domain question
         * @return the response
         */
        public static QuestionResponse from(Question question) {
            List<Long> competencyIds = question.getCompetencies().stream().map(Competency::getId).sorted().toList();
            List<OptionDto> options = null;
            Boolean expectedAnswer = null;
            if (question instanceof MultipleChoiceQuestion multipleChoice) {
                options = multipleChoice.getOptions().stream()
                        .map((AnswerOption option) -> new OptionDto(option.text(), option.correct())).toList();
            } else if (question instanceof TrueFalseQuestion trueFalse) {
                expectedAnswer = trueFalse.isExpectedAnswer();
            }
            QuestionMetadata metadata = question.getMetadata();
            MetadataDto metadataDto = metadata == null ? null : new MetadataDto(metadata.cognitiveLevel(),
                    metadata.estimatedSeconds(), metadata.editorialNotes(), metadata.lastReviewedAt());
            return new QuestionResponse(question.getId(), question.getType(), question.getStatement(),
                    question.getDifficulty(), question.getStatus(), question.getAuthor().getId(),
                    question.getTopic() == null ? null : question.getTopic().getId(), competencyIds, options,
                    expectedAnswer, metadataDto);
        }
    }
}

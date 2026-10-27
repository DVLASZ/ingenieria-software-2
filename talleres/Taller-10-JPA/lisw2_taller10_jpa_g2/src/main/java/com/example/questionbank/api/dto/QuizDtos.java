package com.example.questionbank.api.dto;

import com.example.questionbank.domain.model.Question;
import com.example.questionbank.domain.model.Quiz;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request and response objects for quizzes.
 */
public final class QuizDtos {

    private QuizDtos() {
    }

    /**
     * Body to create a quiz.
     *
     * @param title the title
     * @param ownerId identity of the teacher who owns the quiz
     * @param questionIds identities of the published questions, in order
     */
    public record CreateQuizRequest(@NotBlank @Size(max = 160) String title, @NotNull Long ownerId,
                                    @NotEmpty List<@NotNull Long> questionIds) {
    }

    /**
     * A quiz as the API shows it.
     *
     * @param id identity
     * @param title the title
     * @param ownerId identity of the owner
     * @param questionIds identities of its questions, in order
     */
    public record QuizResponse(Long id, String title, Long ownerId, List<Long> questionIds) {

        /**
         * Builds the response from a domain quiz.
         *
         * @param quiz the domain quiz
         * @return the response
         */
        public static QuizResponse from(Quiz quiz) {
            return new QuizResponse(quiz.getId(), quiz.getTitle(), quiz.getOwner().getId(),
                    quiz.getQuestions().stream().map(Question::getId).toList());
        }
    }
}

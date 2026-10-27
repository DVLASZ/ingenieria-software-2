package com.example.questionbank.api;

import com.example.questionbank.api.dto.QuestionDtos.ChangeStatusRequest;
import com.example.questionbank.api.dto.QuestionDtos.ClassifyRequest;
import com.example.questionbank.api.dto.QuestionDtos.CreateMultipleChoiceRequest;
import com.example.questionbank.api.dto.QuestionDtos.CreateTrueFalseRequest;
import com.example.questionbank.api.dto.QuestionDtos.QuestionResponse;
import com.example.questionbank.application.QuestionService;
import com.example.questionbank.application.command.CreateMultipleChoiceQuestionCommand;
import com.example.questionbank.application.command.CreateTrueFalseQuestionCommand;
import com.example.questionbank.domain.model.AnswerOption;
import com.example.questionbank.domain.model.QuestionStatus;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP endpoints for questions. It only translates HTTP and delegates to the service.
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService service;

    /**
     * Creates the controller.
     *
     * @param service the question use cases
     */
    public QuestionController(QuestionService service) {
        this.service = service;
    }

    /**
     * Creates a multiple choice question.
     *
     * @param request the data of the question
     * @return the stored draft
     */
    @PostMapping("/multiple-choice")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse createMultipleChoice(@Valid @RequestBody CreateMultipleChoiceRequest request) {
        List<AnswerOption> options = request.options().stream()
                .map(option -> new AnswerOption(option.text(), option.correct())).toList();
        return QuestionResponse.from(service.createMultipleChoice(new CreateMultipleChoiceQuestionCommand(
                request.statement(), request.difficulty(), request.authorId(), options,
                request.metadata() == null ? null : request.metadata().toDomain())));
    }

    /**
     * Creates a true/false question.
     *
     * @param request the data of the question
     * @return the stored draft
     */
    @PostMapping("/true-false")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse createTrueFalse(@Valid @RequestBody CreateTrueFalseRequest request) {
        return QuestionResponse.from(service.createTrueFalse(new CreateTrueFalseQuestionCommand(
                request.statement(), request.difficulty(), request.authorId(), request.expectedAnswer(),
                request.metadata() == null ? null : request.metadata().toDomain())));
    }

    /**
     * Finds a question.
     *
     * @param id the identity
     * @return the question
     */
    @GetMapping("/{id}")
    public QuestionResponse get(@PathVariable Long id) {
        return QuestionResponse.from(service.findById(id));
    }

    /**
     * Lists questions, optionally filtered by author and status.
     *
     * @param authorId identity of the author, optional
     * @param status status, optional
     * @return the questions that match
     */
    @GetMapping
    public List<QuestionResponse> list(@RequestParam(required = false) Long authorId,
                                       @RequestParam(required = false) QuestionStatus status) {
        return service.list(authorId, status).stream().map(QuestionResponse::from).toList();
    }

    /**
     * Lists the published questions of a competency.
     *
     * @param competencyId identity of the competency
     * @return the published questions
     */
    @GetMapping("/published")
    public List<QuestionResponse> listPublished(@RequestParam Long competencyId) {
        return service.listPublishedByCompetency(competencyId).stream().map(QuestionResponse::from).toList();
    }

    /**
     * Gives a question a topic and competencies.
     *
     * @param id identity of the question
     * @param request the topic and the competencies
     * @return the stored question
     */
    @PutMapping("/{id}/classification")
    public QuestionResponse classify(@PathVariable Long id, @Valid @RequestBody ClassifyRequest request) {
        return QuestionResponse.from(service.classify(id, request.topicId(), request.competencyIds()));
    }

    /**
     * Sends a draft to review.
     *
     * @param id identity of the question
     * @return the stored question
     */
    @PostMapping("/{id}/submit")
    public QuestionResponse submit(@PathVariable Long id) {
        return QuestionResponse.from(service.submitForReview(id));
    }

    /**
     * Moves a question to another status.
     *
     * @param id identity of the question
     * @param request the new status
     * @return the stored question
     */
    @PostMapping("/{id}/status")
    public QuestionResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeStatusRequest request) {
        return QuestionResponse.from(service.changeStatus(id, request.target()));
    }
}

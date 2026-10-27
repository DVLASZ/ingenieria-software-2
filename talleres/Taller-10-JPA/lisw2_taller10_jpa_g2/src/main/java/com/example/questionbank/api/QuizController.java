package com.example.questionbank.api;

import com.example.questionbank.api.dto.QuizDtos.CreateQuizRequest;
import com.example.questionbank.api.dto.QuizDtos.QuizResponse;
import com.example.questionbank.application.QuizService;
import com.example.questionbank.application.command.CreateQuizCommand;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP endpoints for quizzes.
 */
@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService service;

    /**
     * Creates the controller.
     *
     * @param service the quiz use cases
     */
    public QuizController(QuizService service) {
        this.service = service;
    }

    /**
     * Creates a quiz with published questions.
     *
     * @param request the data of the quiz
     * @return the stored quiz
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse create(@Valid @RequestBody CreateQuizRequest request) {
        return QuizResponse.from(
                service.create(new CreateQuizCommand(request.title(), request.ownerId(), request.questionIds())));
    }

    /**
     * Finds a quiz.
     *
     * @param id the identity
     * @return the quiz
     */
    @GetMapping("/{id}")
    public QuizResponse get(@PathVariable Long id) {
        return QuizResponse.from(service.findById(id));
    }

    /**
     * Lists all quizzes.
     *
     * @return every quiz
     */
    @GetMapping
    public List<QuizResponse> list() {
        return service.list().stream().map(QuizResponse::from).toList();
    }
}

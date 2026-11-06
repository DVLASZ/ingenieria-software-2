package co.edu.unicauca.saberpro.question.api;

import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.question.api.dto.CreateQuestionRequest;
import co.edu.unicauca.saberpro.question.api.dto.QuestionResponse;
import co.edu.unicauca.saberpro.question.api.dto.SubmitAcceptedResponse;
import co.edu.unicauca.saberpro.question.application.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService service;

    public QuestionController(QuestionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> create(@RequestBody CreateQuestionRequest request) {
        Question question = service.create(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(QuestionResponse.from(question));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<SubmitAcceptedResponse> submit(@PathVariable Long id) {
        String operationId = service.submitForReview(id);
        return ResponseEntity.accepted().body(new SubmitAcceptedResponse(operationId, id));
    }
}

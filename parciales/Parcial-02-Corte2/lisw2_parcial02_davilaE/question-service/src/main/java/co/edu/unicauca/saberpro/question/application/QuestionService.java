package co.edu.unicauca.saberpro.question.application;

import co.edu.unicauca.saberpro.domain.builder.IQuestionBuilder;
import co.edu.unicauca.saberpro.domain.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.domain.model.AnswerOption;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.domain.model.QuestionStatus;
import co.edu.unicauca.saberpro.question.application.command.CreateQuestionCommand;
import co.edu.unicauca.saberpro.question.application.command.SubmitQuestionForReview;
import co.edu.unicauca.saberpro.question.application.exception.QuestionNotFoundException;
import co.edu.unicauca.saberpro.question.application.exception.QuestionRejectedException;
import co.edu.unicauca.saberpro.question.application.port.IQuestionRepository;
import co.edu.unicauca.saberpro.question.application.port.ISubmitCommandPublisher;
import co.edu.unicauca.saberpro.question.application.port.IValidationClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class QuestionService {

    private final IQuestionRepository repository;
    private final IValidationClient validationClient;
    private final ISubmitCommandPublisher publisher;

    public QuestionService(IQuestionRepository repository, IValidationClient validationClient,
                           ISubmitCommandPublisher publisher) {
        this.repository = repository;
        this.validationClient = validationClient;
        this.publisher = publisher;
    }

    /** primero pregunta a validation-service por REST; si no pasa, no se guarda nada */
    public Question create(CreateQuestionCommand command) {
        var result = validationClient.validate(command.justification());
        if (!result.valid()) {
            throw new QuestionRejectedException(result.message());
        }
        IQuestionBuilder builder = new QuestionBuilder()
                .statement(command.statement())
                .justification(command.justification());
        for (AnswerOption option : command.options()) {
            builder.addOption(option.text(), option.correct());
        }
        return repository.save(builder.build());
    }

    /** publica el comando y responde ya; el cambio de estado lo hace lifecycle-service */
    public String submitForReview(Long id) {
        Question question = repository.findById(id).orElseThrow(() -> new QuestionNotFoundException(id));
        question.ensureCanSubmit();
        String operationId = UUID.randomUUID().toString();
        publisher.publish(new SubmitQuestionForReview(operationId, question.getId(), question.getStatus().name()));
        return operationId;
    }

    /** lo llama el listener cuando lifecycle-service avisa que el estado cambió */
    public void applyStatus(Long id, QuestionStatus status) {
        Question question = repository.findById(id).orElseThrow(() -> new QuestionNotFoundException(id));
        repository.save(question.withStatus(status));
    }
}

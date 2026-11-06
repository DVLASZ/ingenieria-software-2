package co.edu.unicauca.saberpro.lifecycle.application;

import co.edu.unicauca.saberpro.domain.model.QuestionStatus;
import co.edu.unicauca.saberpro.domain.model.state.IQuestionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LifecycleService {

    private static final Logger log = LoggerFactory.getLogger(LifecycleService.class);

    private final IStatusChangedPublisher publisher;

    public LifecycleService(IStatusChangedPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Hace la transición con el patrón State: el estado actual decide el siguiente, y después
     * avisa a question-service. Lanza InvalidTransitionException si la pregunta ya no era un borrador.
     */
    public QuestionStatusChanged submit(SubmitQuestionForReview command) {
        QuestionStatus current = QuestionStatus.valueOf(command.currentStatus());
        IQuestionState next = current.newState().submitForReview();

        log.info("Pregunta {} pasó de {} a {} (operationId={})", command.questionId(), current, next.status(),
                command.operationId());
        var event = new QuestionStatusChanged(command.questionId(), next.status().name(), command.operationId());
        publisher.publish(event);
        return event;
    }
}

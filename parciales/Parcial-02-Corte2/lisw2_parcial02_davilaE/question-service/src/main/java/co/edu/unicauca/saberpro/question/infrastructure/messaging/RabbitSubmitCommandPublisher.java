package co.edu.unicauca.saberpro.question.infrastructure.messaging;

import co.edu.unicauca.saberpro.question.application.command.SubmitQuestionForReview;
import co.edu.unicauca.saberpro.question.application.exception.MessagingUnavailableException;
import co.edu.unicauca.saberpro.question.application.port.ISubmitCommandPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitSubmitCommandPublisher implements ISubmitCommandPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitSubmitCommandPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitSubmitCommandPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(SubmitQuestionForReview command) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.KEY_SUBMIT, command);
        } catch (AmqpException e) {
            throw new MessagingUnavailableException("No se pudo publicar en RabbitMQ, revisa que esté corriendo", e);
        }
        log.info("Publicado SubmitQuestionForReview operationId={} questionId={}",
                command.operationId(), command.questionId());
    }
}

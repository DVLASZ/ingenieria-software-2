package co.edu.unicauca.question.messaging;

import co.edu.unicauca.question.config.RabbitMQConfig;
import co.edu.unicauca.question.dto.EventEnvelope;
import co.edu.unicauca.question.service.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consume con `@RabbitListener` el resultado de las operaciones que procesó lifecycle-service. */
@Component
public class OperationResultListener {

    private static final Logger log = LoggerFactory.getLogger(OperationResultListener.class);

    private final QuestionService servicio;

    public OperationResultListener(QuestionService servicio) {
        this.servicio = servicio;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_QUESTION_OPERATION_RESULT)
    public void alRecibirElResultado(EventEnvelope mensaje) {
        log.info("[question] resultado de la operación {}: {}", mensaje.stringOf("operationId"), mensaje.stringOf("status"));
        servicio.registrarResultado(mensaje.stringOf("operationId"), mensaje.stringOf("status"), mensaje.stringOf("message"));
    }
}

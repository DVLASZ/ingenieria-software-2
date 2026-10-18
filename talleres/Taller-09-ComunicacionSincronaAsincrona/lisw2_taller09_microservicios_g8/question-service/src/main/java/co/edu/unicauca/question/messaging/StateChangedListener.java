package co.edu.unicauca.question.messaging;

import co.edu.unicauca.question.config.RabbitMQConfig;
import co.edu.unicauca.question.dto.EventEnvelope;
import co.edu.unicauca.question.service.QuestionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Mantiene al día la copia del estado de cada pregunta con los eventos `question.state-changed`. */
@Component
public class StateChangedListener {

    private final QuestionService servicio;

    public StateChangedListener(QuestionService servicio) {
        this.servicio = servicio;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_QUESTION_STATE_CHANGED)
    public void alCambiarElEstado(EventEnvelope mensaje) {
        servicio.actualizarCopiaDelEstado(mensaje.longOf("questionId"), mensaje.stringOf("to"));
    }
}

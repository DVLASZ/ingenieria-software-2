package co.edu.unicauca.saberpro.question.infrastructure.messaging;

import co.edu.unicauca.saberpro.domain.model.QuestionStatus;
import co.edu.unicauca.saberpro.question.application.QuestionService;
import co.edu.unicauca.saberpro.question.application.command.SubmitQuestionForReview;
import co.edu.unicauca.saberpro.question.application.event.QuestionStatusChanged;
import co.edu.unicauca.saberpro.question.application.exception.MessagingUnavailableException;
import co.edu.unicauca.saberpro.question.application.exception.QuestionNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MessagingTest {

    @Test
    void elPublicadorEnviaAlExchangeConLaRoutingKeyCorrecta() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        var command = new SubmitQuestionForReview("op-1", 3L, "DRAFT");

        new RabbitSubmitCommandPublisher(template).publish(command);

        verify(template).convertAndSend("saberpro.direct.exchange", "question.submit", command);
    }

    @Test
    void siRabbitNoRespondeLanzaNoDisponible() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        doThrow(new AmqpConnectException(new java.io.IOException("conexión rechazada")))
                .when(template).convertAndSend(anyString(), anyString(), any(Object.class));

        assertThatThrownBy(() -> new RabbitSubmitCommandPublisher(template)
                .publish(new SubmitQuestionForReview("op-1", 3L, "DRAFT")))
                .isInstanceOf(MessagingUnavailableException.class)
                .hasMessageContaining("RabbitMQ");
    }

    @Test
    void elConversorEscribeElComandoComoJson() {
        Message message = new RabbitConfig().jsonMessageConverter()
                .toMessage(new SubmitQuestionForReview("op-9", 5L, "DRAFT"), new MessageProperties());

        assertThat(new String(message.getBody(), StandardCharsets.UTF_8))
                .contains("\"operationId\":\"op-9\"", "\"questionId\":5", "\"currentStatus\":\"DRAFT\"");
    }

    /** el mensaje trae el nombre de la clase de lifecycle-service, que aquí no existe */
    @Test
    void elConversorLeeElAvisoQueMandaLifecycle() {
        MessageProperties props = new MessageProperties();
        props.setContentType("application/json");
        props.setHeader("__TypeId__", "co.edu.unicauca.saberpro.lifecycle.application.QuestionStatusChanged");
        props.setInferredArgumentType(QuestionStatusChanged.class);
        String json = "{\"questionId\":5,\"status\":\"PENDING_REVIEW\",\"operationId\":\"op-9\"}";

        Object leido = new RabbitConfig().jsonMessageConverter()
                .fromMessage(new Message(json.getBytes(StandardCharsets.UTF_8), props));

        assertThat(leido).isEqualTo(new QuestionStatusChanged(5L, "PENDING_REVIEW", "op-9"));
    }

    @Test
    void elListenerAplicaElNuevoEstado() {
        QuestionService service = mock(QuestionService.class);

        new QuestionStatusChangedListener(service).onStatusChanged(new QuestionStatusChanged(4L, "PENDING_REVIEW", "op-1"));

        verify(service).applyStatus(4L, QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void elListenerNoReencolaUnMensajeQueNoSePuedeAplicar() {
        QuestionService service = mock(QuestionService.class);
        doThrow(new QuestionNotFoundException(4L)).when(service).applyStatus(4L, QuestionStatus.DRAFT);
        var listener = new QuestionStatusChangedListener(service);

        assertThatThrownBy(() -> listener.onStatusChanged(new QuestionStatusChanged(4L, "DRAFT", "op")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
        assertThatThrownBy(() -> listener.onStatusChanged(new QuestionStatusChanged(4L, "NO_EXISTE", "op")))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }

    @Test
    void laTopologiaDelAvisoUsaSuPropiaCola() {
        RabbitConfig config = new RabbitConfig();
        Queue queue = config.statusChangedQueue();
        Binding binding = config.statusChangedBinding(queue, config.saberproExchange());

        assertThat(queue.getName()).isEqualTo("question.status.changed.queue");
        assertThat(binding.getRoutingKey()).isEqualTo("question.status.changed");
    }

    @Test
    void laTopologiaUsaLosNombresDelEnunciado() {
        RabbitConfig config = new RabbitConfig();
        DirectExchange exchange = config.saberproExchange();
        Queue queue = config.submitQueue();
        Binding binding = config.submitBinding(queue, exchange);

        assertThat(exchange.getName()).isEqualTo("saberpro.direct.exchange");
        assertThat(queue.getName()).isEqualTo("question.submit.review.queue");
        assertThat(binding.getRoutingKey()).isEqualTo("question.submit");
        assertThat(binding.getExchange()).isEqualTo("saberpro.direct.exchange");
        assertThat(binding.getDestination()).isEqualTo("question.submit.review.queue");
    }
}

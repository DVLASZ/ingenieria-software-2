package co.edu.unicauca.saberpro.lifecycle.infrastructure.messaging;

import co.edu.unicauca.saberpro.lifecycle.application.QuestionStatusChanged;
import co.edu.unicauca.saberpro.lifecycle.application.SubmitQuestionForReview;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitConfigTest {

    /** el mensaje trae el nombre de la clase de question-service, que aquí no existe */
    @Test
    void elConversorLeeElComandoQueMandaQuestionService() {
        MessageProperties props = new MessageProperties();
        props.setContentType("application/json");
        props.setHeader("__TypeId__", "co.edu.unicauca.saberpro.question.application.command.SubmitQuestionForReview");
        props.setInferredArgumentType(SubmitQuestionForReview.class);
        String json = "{\"operationId\":\"op-9\",\"questionId\":5,\"currentStatus\":\"DRAFT\"}";

        Object leido = new RabbitConfig().jsonMessageConverter()
                .fromMessage(new Message(json.getBytes(StandardCharsets.UTF_8), props));

        assertThat(leido).isEqualTo(new SubmitQuestionForReview("op-9", 5L, "DRAFT"));
    }

    @Test
    void elConversorEscribeElAvisoComoJson() {
        Message message = new RabbitConfig().jsonMessageConverter()
                .toMessage(new QuestionStatusChanged(5L, "PENDING_REVIEW", "op-9"), new MessageProperties());

        assertThat(new String(message.getBody(), StandardCharsets.UTF_8))
                .contains("\"questionId\":5", "\"status\":\"PENDING_REVIEW\"", "\"operationId\":\"op-9\"");
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
        assertThat(binding.getDestination()).isEqualTo("question.submit.review.queue");
    }
}

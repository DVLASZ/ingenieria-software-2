package co.edu.unicauca.saberpro.question.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara el exchange, las dos colas y sus bindings. Lifecycle-service declara el exchange y la cola
 * del comando también (es idempotente), así el mensaje no se pierde sin importar cuál arranque primero.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "saberpro.direct.exchange";
    public static final String KEY_SUBMIT = "question.submit";
    public static final String QUEUE_SUBMIT = "question.submit.review.queue";
    public static final String KEY_STATUS_CHANGED = "question.status.changed";
    public static final String QUEUE_STATUS_CHANGED = "question.status.changed.queue";

    @Bean
    DirectExchange saberproExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue submitQueue() {
        return QueueBuilder.durable(QUEUE_SUBMIT).build();
    }

    @Bean
    Queue statusChangedQueue() {
        return QueueBuilder.durable(QUEUE_STATUS_CHANGED).build();
    }

    @Bean
    Binding submitBinding(Queue submitQueue, DirectExchange saberproExchange) {
        return BindingBuilder.bind(submitQueue).to(saberproExchange).with(KEY_SUBMIT);
    }

    @Bean
    Binding statusChangedBinding(Queue statusChangedQueue, DirectExchange saberproExchange) {
        return BindingBuilder.bind(statusChangedQueue).to(saberproExchange).with(KEY_STATUS_CHANGED);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

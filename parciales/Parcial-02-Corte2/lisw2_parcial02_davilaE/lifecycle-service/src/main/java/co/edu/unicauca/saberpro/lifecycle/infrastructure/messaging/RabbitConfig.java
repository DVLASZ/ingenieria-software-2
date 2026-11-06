package co.edu.unicauca.saberpro.lifecycle.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** mismo exchange y misma cola que declara question-service; declararlos dos veces es inofensivo */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "saberpro.direct.exchange";
    public static final String KEY_SUBMIT = "question.submit";
    public static final String QUEUE_SUBMIT = "question.submit.review.queue";
    public static final String KEY_STATUS_CHANGED = "question.status.changed";

    @Bean
    DirectExchange saberproExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue submitQueue() {
        return QueueBuilder.durable(QUEUE_SUBMIT).build();
    }

    @Bean
    Binding submitBinding(Queue submitQueue, DirectExchange saberproExchange) {
        return BindingBuilder.bind(submitQueue).to(saberproExchange).with(KEY_SUBMIT);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

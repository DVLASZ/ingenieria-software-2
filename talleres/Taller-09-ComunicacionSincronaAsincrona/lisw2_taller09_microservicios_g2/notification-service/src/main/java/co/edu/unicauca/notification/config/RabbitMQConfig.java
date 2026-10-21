package co.edu.unicauca.notification.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ de este servicio: los dos exchanges (comandos y eventos), las colas que consume,
 * sus bindings y la serialización JSON. Cada servicio tiene su propia copia, a propósito.
 */
@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NOTIFICATION_REVIEWER_ASSIGNED = "notification.reviewer-assigned";
    public static final String QUEUE_NOTIFICATION_EVENTS = "notification.events";

    @Value("${app.messaging.direct-exchange}")
    private String directExchangeName;

    @Value("${app.messaging.topic-exchange}")
    private String topicExchangeName;

    @Bean
    DirectExchange directExchange() {
        return new DirectExchange(directExchangeName, true, false);
    }

    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange(topicExchangeName, true, false);
    }

    @Bean
    Queue qNotificationReviewerAssigned() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION_REVIEWER_ASSIGNED).build();
    }

    @Bean
    Declarables bindingsNotificationReviewerAssigned() {
        return new Declarables(
                BindingBuilder.bind(qNotificationReviewerAssigned()).to(topicExchange()).with("reviewer.assigned"));
    }

    @Bean
    Queue qNotificationEvents() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION_EVENTS).build();
    }

    @Bean
    Declarables bindingsNotificationEvents() {
        return new Declarables(
                BindingBuilder.bind(qNotificationEvents()).to(topicExchange()).with("review.evaluation-submitted"),
                BindingBuilder.bind(qNotificationEvents()).to(topicExchange()).with("review.completed"),
                BindingBuilder.bind(qNotificationEvents()).to(topicExchange()).with("validation.failed"),
                BindingBuilder.bind(qNotificationEvents()).to(topicExchange()).with("question.published"));
    }

    /** Los mensajes viajan en JSON; el tipo se toma del parámetro del listener, no del servicio que publicó. */
    @Bean
    MessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper mapper = new DefaultJackson2JavaTypeMapper();
        mapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        mapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(mapper);
        return converter;
    }
}

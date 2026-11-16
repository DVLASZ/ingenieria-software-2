package co.edu.unicauca.saberpro.question.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** El exchange y las dos colas se declaran en los dos servicios. Declararlos dos veces no hace daño. */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "saberpro.direct.exchange";
    public static final String KEY_REVISAR = "pregunta.revisar";
    public static final String QUEUE_REVISAR = "pregunta.revisar.queue";
    public static final String KEY_ESTADO = "pregunta.estado.cambiado";
    public static final String QUEUE_ESTADO = "pregunta.estado.cambiado.queue";

    @Bean
    DirectExchange saberproExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue revisarQueue() {
        return QueueBuilder.durable(QUEUE_REVISAR).build();
    }

    @Bean
    Queue estadoQueue() {
        return QueueBuilder.durable(QUEUE_ESTADO).build();
    }

    @Bean
    Binding revisarBinding(Queue revisarQueue, DirectExchange saberproExchange) {
        return BindingBuilder.bind(revisarQueue).to(saberproExchange).with(KEY_REVISAR);
    }

    @Bean
    Binding estadoBinding(Queue estadoQueue, DirectExchange saberproExchange) {
        return BindingBuilder.bind(estadoQueue).to(saberproExchange).with(KEY_ESTADO);
    }

    // el tipo del mensaje se toma del parámetro del listener, así los dos servicios no comparten clases
    @Bean
    MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper mapper = new DefaultJackson2JavaTypeMapper();
        mapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        mapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(mapper);
        return converter;
    }
}

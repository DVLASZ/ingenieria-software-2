package co.edu.unicauca.saberpro.lifecycle;

import co.edu.unicauca.saberpro.lifecycle.infrastructure.messaging.SubmitQuestionListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** que el contexto arranque sin broker (el listener queda apagado en el test) */
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class LifecycleApplicationTest {

    @Autowired
    SubmitQuestionListener listener;

    @Test
    void elContextoArranca() {
        assertThat(listener).isNotNull();
    }
}

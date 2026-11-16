package co.edu.unicauca.saberpro.question.api;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.JpaQuestionAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"saberpro.persistencia=jpa", "spring.rabbitmq.listener.simple.auto-startup=false"})
class FlujoRestConJpaTest extends FlujoRestBaseTest {

    @Autowired
    PreguntaRepositoryPort puerto;

    @Test
    void elAdaptadorActivoEsJpa() {
        assertInstanceOf(JpaQuestionAdapter.class, puerto);
    }
}

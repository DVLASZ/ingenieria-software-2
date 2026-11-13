package co.edu.unicauca.saberpro.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa.JpaQuestionAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "saberpro.persistencia=jpa")
class FlujoRestConJpaTest extends FlujoRestBaseTest {

    @Autowired
    PreguntaRepositoryPort puerto;

    @Test
    void elAdaptadorActivoEsJpa() {
        assertInstanceOf(JpaQuestionAdapter.class, puerto);
    }
}

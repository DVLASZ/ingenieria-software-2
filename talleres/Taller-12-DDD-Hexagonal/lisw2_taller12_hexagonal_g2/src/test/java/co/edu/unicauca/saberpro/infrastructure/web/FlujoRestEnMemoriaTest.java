package co.edu.unicauca.saberpro.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.memory.InMemoryQuestionAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "saberpro.persistencia=memoria")
class FlujoRestEnMemoriaTest extends FlujoRestBaseTest {

    @Autowired
    PreguntaRepositoryPort puerto;

    @Test
    void elAdaptadorActivoEsElDeMemoria() {
        assertInstanceOf(InMemoryQuestionAdapter.class, puerto);
    }
}

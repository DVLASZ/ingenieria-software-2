package co.edu.unicauca.saberpro.question.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.QuestionDomainMapper;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.QuestionRecord;
import org.junit.jupiter.api.Test;

class QuestionDomainMapperTest {

    final QuestionDomainMapper mapper = new QuestionDomainMapper();

    @Test
    void idaYVueltaConservaTodo() {
        Pregunta original = PreguntaRepositoryPortContractTest.nueva();
        QuestionRecord registro = mapper.toRecord(original);
        assertEquals(original.id(), registro.getId());
        assertEquals(4, registro.getOpciones().size());
        Pregunta vuelta = mapper.toDomain(registro);
        assertEquals(original.id(), vuelta.id());
        assertEquals(original.enunciado(), vuelta.enunciado());
        assertEquals(original.justificacion(), vuelta.justificacion());
        assertEquals(original.opciones(), vuelta.opciones());
        assertEquals(EstadoPregunta.BORRADOR, vuelta.estado());
    }
}

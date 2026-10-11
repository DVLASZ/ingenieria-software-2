package co.edu.unicauca.saberpro.domain;

import co.edu.unicauca.saberpro.adapter.ExternalQuestionAdapter;
import co.edu.unicauca.saberpro.adapter.ExternalQuestionService;
import co.edu.unicauca.saberpro.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.builder.QuestionDirector;
import co.edu.unicauca.saberpro.domain.model.Competence;
import co.edu.unicauca.saberpro.domain.model.Difficulty;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.domain.repository.InMemoryQuestionRepository;
import co.edu.unicauca.saberpro.domain.service.QuestionService;
import co.edu.unicauca.saberpro.domain.validation.QuestionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Subsistemas del banco: validador, repositorio y servicio")
class QuestionServiceTest {

    private QuestionService service;
    private QuestionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new QuestionValidator();
        service = new QuestionService(new InMemoryQuestionRepository(), validator);
    }

    private Question pregunta(String enunciado, String... opciones) {
        return new QuestionDirector(new QuestionBuilder()).buildMultipleChoice("Contexto", enunciado,
                List.of(opciones), 0, "Justificación", Competence.ENGLISH, "Tema", "Subtema", Difficulty.BASIC);
    }

    @Test
    @DisplayName("el validador acepta una pregunta bien formulada")
    void validadorAcepta() {
        assertTrue(validator.isValid(pregunta("¿Cuál es correcta?", "a", "b", "c", "d")));
    }

    @Test
    @DisplayName("el validador rechaza un enunciado sin signo de interrogación")
    void validadorRechazaSinInterrogacion() {
        Question question = pregunta("Diga cuál es correcta.", "a", "b", "c", "d");

        assertTrue(validator.validate(question).get(0).contains("0 signos de interrogación"));
    }

    @Test
    @DisplayName("el validador rechaza un enunciado con dos preguntas")
    void validadorRechazaDosPreguntas() {
        Question question = pregunta("¿Cuál? ¿Y esta?", "a", "b", "c", "d");

        assertFalse(validator.isValid(question));
    }

    @Test
    @DisplayName("el validador rechaza opciones repetidas sin importar mayúsculas")
    void validadorRechazaOpcionesRepetidas() {
        Question question = pregunta("¿Cuál?", "Uno", "uno", "c", "d");

        assertTrue(validator.validate(question).get(0).contains("repite el texto"));
    }

    @Test
    @DisplayName("el servicio registra una pregunta válida y se puede consultar")
    void registraYConsulta() {
        Question question = service.register(pregunta("¿Cuál?", "a", "b", "c", "d"));

        assertEquals(question, service.findById(question.getId()).orElseThrow());
        assertEquals(1, service.findAll().size());
    }

    @Test
    @DisplayName("el servicio no registra una pregunta inválida")
    void noRegistraInvalida() {
        Question invalida = pregunta("Sin interrogación", "a", "b", "c", "d");

        assertThrows(IllegalArgumentException.class, () -> service.register(invalida));
        assertTrue(service.findAll().isEmpty());
    }

    @Test
    @DisplayName("buscar un id que no existe da un resultado vacío")
    void idInexistente() {
        assertTrue(service.findById("no-existe").isEmpty());
    }

    @Test
    @DisplayName("importar desde el adaptador registra las preguntas válidas del servicio externo")
    void importaDesdeAdaptador() {
        int imported = service.importFrom(new ExternalQuestionAdapter(new ExternalQuestionService()));

        assertEquals(2, imported);
        assertEquals(2, service.findAll().size());
    }

    @Test
    @DisplayName("importar omite las preguntas que no pasan el validador")
    void importaOmiteInvalidas() {
        ExternalQuestionService externo = new ExternalQuestionService(List.of(
                "C|Sin interrogación|a;b;c;d|A|J|ENGLISH|T|S|BASIC",
                "C|¿Con interrogación?|a;b;c;d|A|J|ENGLISH|T|S|BASIC"));

        int imported = service.importFrom(new ExternalQuestionAdapter(externo));

        assertEquals(1, imported);
    }

    @Test
    @DisplayName("el repositorio conserva el orden en que se guardaron las preguntas")
    void conservaElOrden() {
        Question primera = service.register(pregunta("¿Primera?", "a", "b", "c", "d"));
        Question segunda = service.register(pregunta("¿Segunda?", "a", "b", "c", "d"));

        assertEquals(List.of(primera, segunda), service.findAll());
    }
}

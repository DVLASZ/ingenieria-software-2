package co.edu.unicauca.saberpro.facade;

import co.edu.unicauca.saberpro.adapter.ExternalQuestionAdapter;
import co.edu.unicauca.saberpro.adapter.ExternalQuestionService;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Patrón Facade: QuestionBankFacade")
class QuestionBankFacadeTest {

    private QuestionBankFacade bank;

    @BeforeEach
    void setUp() {
        bank = new QuestionBankFacade();
    }

    private Question crear(String enunciado, List<String> opciones, int correcta) {
        return bank.createQuestion("Contexto", enunciado, opciones, correcta, "Justificación",
                Competence.ENGLISH, "Tema", "Subtema", Difficulty.BASIC);
    }

    private QuestionBankFacade bancoConExterno(ExternalQuestionService externo) {
        return new QuestionBankFacade(
                new QuestionService(new InMemoryQuestionRepository(), new QuestionValidator()),
                new ExternalQuestionAdapter(externo));
    }

    @Test
    @DisplayName("crea una pregunta válida y la deja consultable por id")
    void creaYConsulta() {
        Question question = crear("¿Cuál es correcta?", List.of("a", "b", "c", "d"), 2);

        assertEquals('C', question.getCorrectOption().label());
        assertEquals(question, bank.findQuestion(question.getId()).orElseThrow());
    }

    @Test
    @DisplayName("rechaza con QuestionBankException una pregunta con opciones incorrectas")
    void rechazaOpcionesIncorrectas() {
        QuestionBankException error = assertThrows(QuestionBankException.class,
                () -> crear("¿Cuál?", List.of("a", "b"), 0));

        assertTrue(error.getMessage().contains("exactamente 4 opciones"));
        assertTrue(bank.listQuestions().isEmpty());
    }

    @Test
    @DisplayName("rechaza con QuestionBankException un enunciado sin signo de interrogación")
    void rechazaSinInterrogacion() {
        QuestionBankException error = assertThrows(QuestionBankException.class,
                () -> crear("Diga cuál es correcta.", List.of("a", "b", "c", "d"), 0));

        assertTrue(error.getMessage().contains("signos de interrogación"));
        assertTrue(bank.listQuestions().isEmpty());
    }

    @Test
    @DisplayName("el banco nuevo está vacío")
    void bancoVacio() {
        assertTrue(bank.listQuestions().isEmpty());
    }

    @Test
    @DisplayName("importa 2 preguntas del servicio de ejemplo y reporta la descartada con su motivo")
    void importaServicioDeEjemplo() {
        ImportSummary summary = bank.importExternalQuestions();

        assertEquals(2, summary.imported());
        assertEquals(1, summary.rejected().size());
        assertTrue(summary.rejected().get(0).contains("Pregunta 3 descartada"));
        assertEquals(2, bank.listQuestions().size());
    }

    @Test
    @DisplayName("lo importado se suma a lo creado a mano")
    void importadoSeSumaAlBanco() {
        crear("¿Propia?", List.of("a", "b", "c", "d"), 0);

        bank.importExternalQuestions();

        assertEquals(3, bank.listQuestions().size());
    }

    @Test
    @DisplayName("una pregunta externa que el validador rechaza queda en el resumen y no en el banco")
    void importaDescartaLasQueNoPasanElValidador() {
        QuestionBankFacade conExterno = bancoConExterno(new ExternalQuestionService(List.of(
                "C|Sin interrogación|a;b;c;d|A|J|ENGLISH|T|S|BASIC",
                "C|¿Con interrogación?|a;b;c;d|A|J|ENGLISH|T|S|BASIC")));

        ImportSummary summary = conExterno.importExternalQuestions();

        assertEquals(1, summary.imported());
        assertEquals(1, summary.rejected().size());
        assertTrue(summary.rejected().get(0).contains("signos de interrogación"));
        assertEquals(1, conExterno.listQuestions().size());
    }

    @Test
    @DisplayName("importar con un servicio vacío no registra ni descarta nada")
    void importaServicioVacio() {
        ImportSummary summary = bancoConExterno(new ExternalQuestionService(List.of())).importExternalQuestions();

        assertEquals(0, summary.imported());
        assertTrue(summary.rejected().isEmpty());
    }

    @Test
    @DisplayName("buscar un id inexistente da un resultado vacío")
    void idInexistente() {
        assertTrue(bank.findQuestion("no-existe").isEmpty());
    }

    @Test
    @DisplayName("el resumen de importación no se puede modificar desde afuera")
    void resumenInmodificable() {
        ImportSummary summary = bank.importExternalQuestions();

        assertThrows(UnsupportedOperationException.class, () -> summary.rejected().add("x"));
    }
}

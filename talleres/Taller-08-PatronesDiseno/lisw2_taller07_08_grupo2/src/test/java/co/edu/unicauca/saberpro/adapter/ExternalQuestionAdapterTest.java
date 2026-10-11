package co.edu.unicauca.saberpro.adapter;

import co.edu.unicauca.saberpro.domain.model.Competence;
import co.edu.unicauca.saberpro.domain.model.Difficulty;
import co.edu.unicauca.saberpro.domain.model.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Patrón Adapter: ExternalQuestionAdapter")
class ExternalQuestionAdapterTest {

    private static final String VALIDA = "Contexto|¿Cuál es correcta?|Uno;Dos;Tres;Cuatro|B|Justificación|"
            + "CRITICAL_READING|Tema|Subtema|BASIC";

    private ExternalQuestionAdapter adapterCon(String... raws) {
        return new ExternalQuestionAdapter(new ExternalQuestionService(List.of(raws)));
    }

    @Test
    @DisplayName("el adaptador cumple la interfaz que espera el banco")
    void cumpleLaInterfazObjetivo() {
        assertInstanceOf(QuestionProvider.class, adapterCon(VALIDA));
    }

    @Test
    @DisplayName("convierte una cadena cruda en una pregunta con todos sus campos")
    void conviertePreguntaValida() {
        List<Question> questions = adapterCon(VALIDA).getQuestions();

        assertEquals(1, questions.size());
        Question question = questions.get(0);
        assertEquals("Contexto", question.getContext().text());
        assertEquals("¿Cuál es correcta?", question.getStatement());
        assertEquals(4, question.getOptions().size());
        assertEquals("Dos", question.getCorrectOption().text());
        assertEquals('B', question.getCorrectOption().label());
        assertEquals("Justificación", question.getJustification());
        assertEquals(Competence.CRITICAL_READING, question.getClassification().competence());
        assertEquals("Tema", question.getClassification().topic());
        assertEquals("Subtema", question.getClassification().subtopic());
        assertEquals(Difficulty.BASIC, question.getClassification().difficulty());
    }

    @Test
    @DisplayName("acepta la letra correcta en minúscula y los nombres de enumeración con otras mayúsculas")
    void toleraMayusculas() {
        String raw = "Contexto|¿Pregunta?|a;b;c;d|d|J|english|T|S|advanced";

        Question question = adapterCon(raw).getQuestions().get(0);

        assertEquals('D', question.getCorrectOption().label());
        assertEquals(Competence.ENGLISH, question.getClassification().competence());
        assertEquals(Difficulty.ADVANCED, question.getClassification().difficulty());
    }

    @Test
    @DisplayName("con el servicio de ejemplo importa 2 preguntas y descarta la que solo trae 3 opciones")
    void servicioDeEjemplo() {
        ExternalQuestionAdapter adapter = new ExternalQuestionAdapter(new ExternalQuestionService());

        List<Question> questions = adapter.getQuestions();

        assertEquals(2, questions.size());
        assertEquals(1, adapter.getRejected().size());
        assertTrue(adapter.getRejected().get(0).contains("Pregunta 3 descartada"));
        assertTrue(adapter.getRejected().get(0).contains("tiene 3"));
    }

    @Test
    @DisplayName("descarta una cadena con un número incorrecto de campos")
    void descartaCamposIncorrectos() {
        ExternalQuestionAdapter adapter = adapterCon("solo|tres|campos");

        assertTrue(adapter.getQuestions().isEmpty());
        assertTrue(adapter.getRejected().get(0).contains("tiene 3 campos y se esperaban 9"));
    }

    @Test
    @DisplayName("descarta una cadena con una letra de respuesta inválida")
    void descartaLetraInvalida() {
        String raw = "C|¿P?|a;b;c;d|7|J|ENGLISH|T|S|BASIC";
        ExternalQuestionAdapter adapter = adapterCon(raw);

        assertTrue(adapter.getQuestions().isEmpty());
        assertTrue(adapter.getRejected().get(0).contains("letra de la respuesta correcta"));
    }

    @Test
    @DisplayName("descarta una cadena cuya letra correcta no corresponde a ninguna opción")
    void descartaLetraFueraDeRango() {
        String raw = "C|¿P?|a;b;c;d|Z|J|ENGLISH|T|S|BASIC";
        ExternalQuestionAdapter adapter = adapterCon(raw);

        assertTrue(adapter.getQuestions().isEmpty());
        assertTrue(adapter.getRejected().get(0).contains("1 respuesta correcta y tiene 0"));
    }

    @Test
    @DisplayName("descarta una cadena con una competencia desconocida")
    void descartaCompetenciaDesconocida() {
        String raw = "C|¿P?|a;b;c;d|A|J|MATEMATICAS|T|S|BASIC";
        ExternalQuestionAdapter adapter = adapterCon(raw);

        assertTrue(adapter.getQuestions().isEmpty());
        assertTrue(adapter.getRejected().get(0).contains("valor desconocido para Competence"));
    }

    @Test
    @DisplayName("descarta una cadena con una dificultad desconocida")
    void descartaDificultadDesconocida() {
        String raw = "C|¿P?|a;b;c;d|A|J|ENGLISH|T|S|IMPOSIBLE";
        ExternalQuestionAdapter adapter = adapterCon(raw);

        assertTrue(adapter.getQuestions().isEmpty());
        assertTrue(adapter.getRejected().get(0).contains("valor desconocido para Difficulty"));
    }

    @Test
    @DisplayName("una cadena mala no detiene la importación de las demás")
    void sigueDespuesDeUnError() {
        ExternalQuestionAdapter adapter = adapterCon("mala", VALIDA, "otra mala");

        assertEquals(1, adapter.getQuestions().size());
        assertEquals(2, adapter.getRejected().size());
        assertTrue(adapter.getRejected().get(0).startsWith("Pregunta 1"));
        assertTrue(adapter.getRejected().get(1).startsWith("Pregunta 3"));
    }

    @Test
    @DisplayName("el registro de descartes se reinicia en cada importación")
    void descartesSeReinician() {
        ExternalQuestionAdapter adapter = adapterCon("mala");

        adapter.getQuestions();
        adapter.getQuestions();

        assertEquals(1, adapter.getRejected().size());
    }

    @Test
    @DisplayName("un servicio sin preguntas da una lista vacía y ningún descarte")
    void servicioVacio() {
        ExternalQuestionAdapter adapter = new ExternalQuestionAdapter(new ExternalQuestionService(List.of()));

        assertTrue(adapter.getQuestions().isEmpty());
        assertTrue(adapter.getRejected().isEmpty());
    }

    @Test
    @DisplayName("descarta una pregunta con una opción vacía")
    void descartaOpcionVacia() {
        String raw = "C|¿P?|a;;c;d|A|J|ENGLISH|T|S|BASIC";
        ExternalQuestionAdapter adapter = adapterCon(raw);

        assertTrue(adapter.getQuestions().isEmpty());
        assertTrue(adapter.getRejected().get(0).contains("ninguna opción puede estar vacía"));
    }
}

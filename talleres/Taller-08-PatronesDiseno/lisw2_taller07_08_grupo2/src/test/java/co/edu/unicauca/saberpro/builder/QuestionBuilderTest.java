package co.edu.unicauca.saberpro.builder;

import co.edu.unicauca.saberpro.domain.model.Competence;
import co.edu.unicauca.saberpro.domain.model.Difficulty;
import co.edu.unicauca.saberpro.domain.model.Option;
import co.edu.unicauca.saberpro.domain.model.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Patrón Builder: QuestionBuilder y QuestionDirector")
class QuestionBuilderTest {

    private QuestionBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new QuestionBuilder();
    }

    /** Deja el builder con todas las partes correctas menos las opciones, que se agregan en cada prueba. */
    private IQuestionBuilder baseSinOpciones() {
        return builder.setContext("Contexto de la pregunta")
                .setStatement("¿Cuál es la respuesta?")
                .setJustification("Porque sí")
                .setClassification(Competence.CRITICAL_READING, "Tema", "Subtema", Difficulty.BASIC);
    }

    private Question construirValida() {
        return baseSinOpciones()
                .addOption("Uno", false).addOption("Dos", true).addOption("Tres", false).addOption("Cuatro", false)
                .build();
    }

    @Test
    @DisplayName("construye una pregunta completa con las partes que se le dieron")
    void construyePreguntaCompleta() {
        Question question = construirValida();

        assertEquals("Contexto de la pregunta", question.getContext().text());
        assertEquals("¿Cuál es la respuesta?", question.getStatement());
        assertEquals("Porque sí", question.getJustification());
        assertEquals(Competence.CRITICAL_READING, question.getClassification().competence());
        assertEquals("Tema", question.getClassification().topic());
        assertEquals("Subtema", question.getClassification().subtopic());
        assertEquals(Difficulty.BASIC, question.getClassification().difficulty());
        assertEquals(4, question.getOptions().size());
    }

    @Test
    @DisplayName("asigna las letras A, B, C y D en el orden en que se agregan las opciones")
    void asignaLetras() {
        List<Option> options = construirValida().getOptions();

        assertEquals('A', options.get(0).label());
        assertEquals('B', options.get(1).label());
        assertEquals('C', options.get(2).label());
        assertEquals('D', options.get(3).label());
    }

    @Test
    @DisplayName("la opción marcada como correcta es la que devuelve getCorrectOption")
    void opcionCorrecta() {
        Option correct = construirValida().getCorrectOption();

        assertEquals('B', correct.label());
        assertEquals("Dos", correct.text());
    }

    @Test
    @DisplayName("los pasos se pueden dar en cualquier orden")
    void pasosEnCualquierOrden() {
        Question question = builder
                .setClassification(Competence.ENGLISH, "Grammar", "Tenses", Difficulty.ADVANCED)
                .addOption("a", true).addOption("b", false)
                .setJustification("J")
                .addOption("c", false).addOption("d", false)
                .setStatement("¿Pregunta?")
                .setContext("C")
                .build();

        assertEquals("¿Pregunta?", question.getStatement());
        assertEquals('A', question.getCorrectOption().label());
    }

    @Test
    @DisplayName("rechaza una pregunta con menos de 4 opciones")
    void rechazaTresOpciones() {
        IQuestionBuilder incompleto = baseSinOpciones()
                .addOption("Uno", true).addOption("Dos", false).addOption("Tres", false);

        InvalidQuestionException error = assertThrows(InvalidQuestionException.class, incompleto::build);

        assertTrue(error.getProblems().stream().anyMatch(p -> p.contains("exactamente 4 opciones y tiene 3")));
    }

    @Test
    @DisplayName("rechaza una pregunta con más de 4 opciones")
    void rechazaCincoOpciones() {
        IQuestionBuilder demasiado = baseSinOpciones()
                .addOption("1", true).addOption("2", false).addOption("3", false)
                .addOption("4", false).addOption("5", false);

        InvalidQuestionException error = assertThrows(InvalidQuestionException.class, demasiado::build);

        assertTrue(error.getProblems().stream().anyMatch(p -> p.contains("tiene 5")));
    }

    @Test
    @DisplayName("rechaza una pregunta sin ninguna respuesta correcta")
    void rechazaSinCorrecta() {
        IQuestionBuilder sinCorrecta = baseSinOpciones()
                .addOption("1", false).addOption("2", false).addOption("3", false).addOption("4", false);

        InvalidQuestionException error = assertThrows(InvalidQuestionException.class, sinCorrecta::build);

        assertTrue(error.getProblems().stream().anyMatch(p -> p.contains("1 respuesta correcta y tiene 0")));
    }

    @Test
    @DisplayName("rechaza una pregunta con dos respuestas correctas")
    void rechazaDosCorrectas() {
        IQuestionBuilder dosCorrectas = baseSinOpciones()
                .addOption("1", true).addOption("2", true).addOption("3", false).addOption("4", false);

        InvalidQuestionException error = assertThrows(InvalidQuestionException.class, dosCorrectas::build);

        assertTrue(error.getProblems().stream().anyMatch(p -> p.contains("1 respuesta correcta y tiene 2")));
    }

    @Test
    @DisplayName("rechaza una pregunta vacía y lista todos los incumplimientos de una vez")
    void listaTodosLosProblemas() {
        InvalidQuestionException error = assertThrows(InvalidQuestionException.class, builder::build);

        assertTrue(error.getProblems().size() >= 5, "se esperaban todos los problemas: " + error.getProblems());
        assertTrue(error.getMessage().startsWith("Pregunta inválida"));
    }

    @Test
    @DisplayName("rechaza una opción con texto vacío")
    void rechazaOpcionVacia() {
        IQuestionBuilder conVacia = baseSinOpciones()
                .addOption("1", true).addOption(" ", false).addOption("3", false).addOption("4", false);

        InvalidQuestionException error = assertThrows(InvalidQuestionException.class, conVacia::build);

        assertTrue(error.getProblems().stream().anyMatch(p -> p.contains("ninguna opción puede estar vacía")));
    }

    @Test
    @DisplayName("rechaza una pregunta sin contexto, enunciado, justificación o clasificación")
    void rechazaPartesFaltantes() {
        IQuestionBuilder soloOpciones = builder
                .addOption("1", true).addOption("2", false).addOption("3", false).addOption("4", false);

        InvalidQuestionException error = assertThrows(InvalidQuestionException.class, soloOpciones::build);

        assertEquals(4, error.getProblems().size(), error.getProblems().toString());
    }

    @Test
    @DisplayName("después de construir una pregunta el builder queda vacío para la siguiente")
    void seReiniciaAlConstruir() {
        construirValida();

        assertThrows(InvalidQuestionException.class, builder::build);
    }

    @Test
    @DisplayName("reset descarta lo acumulado")
    void resetDescarta() {
        baseSinOpciones().addOption("1", true);
        builder.reset();

        InvalidQuestionException error = assertThrows(InvalidQuestionException.class, builder::build);

        assertTrue(error.getProblems().stream().anyMatch(p -> p.contains("falta el contexto")));
    }

    @Test
    @DisplayName("el builder se puede reutilizar para varias preguntas distintas")
    void variasPreguntasSeguidas() {
        Question primera = construirValida();
        Question segunda = construirValida();

        assertEquals(primera.getStatement(), segunda.getStatement());
        assertTrue(!primera.getId().equals(segunda.getId()), "cada pregunta tiene su propio id");
    }

    @Test
    @DisplayName("la lista de opciones de la pregunta construida no se puede modificar")
    void opcionesInmodificables() {
        Question question = construirValida();

        assertThrows(UnsupportedOperationException.class, () -> question.getOptions().clear());
    }

    @Test
    @DisplayName("el director arma una pregunta con el orden estándar de pasos")
    void directorArmaPregunta() {
        QuestionDirector director = new QuestionDirector(builder);

        Question question = director.buildMultipleChoice("Contexto", "¿Pregunta?",
                List.of("a", "b", "c", "d"), 2, "Justificación",
                Competence.CITIZENSHIP, "Constitución", "Derechos", Difficulty.INTERMEDIATE);

        assertEquals('C', question.getCorrectOption().label());
        assertEquals(Competence.CITIZENSHIP, question.getClassification().competence());
    }

    @Test
    @DisplayName("el director propaga el error si las opciones no son 4")
    void directorConOpcionesIncorrectas() {
        QuestionDirector director = new QuestionDirector(builder);

        assertThrows(InvalidQuestionException.class, () -> director.buildMultipleChoice("C", "¿P?",
                List.of("a", "b"), 0, "J", Competence.ENGLISH, "T", "S", Difficulty.BASIC));
    }

    @Test
    @DisplayName("la pregunta de ejemplo del director es válida y su respuesta correcta es Observer")
    void preguntaDeEjemplo() {
        Question sample = new QuestionDirector(builder).buildSampleQuestion();

        assertEquals("Observer", sample.getCorrectOption().text());
        assertEquals(4, sample.getOptions().size());
    }
}

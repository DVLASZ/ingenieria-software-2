package co.edu.unicauca.saberpro.decorator;

import co.edu.unicauca.saberpro.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.builder.QuestionDirector;
import co.edu.unicauca.saberpro.domain.model.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Patrón Decorator: BasicQuestion, HighDifficultyQuestion y FeedbackQuestion")
class QuestionDecoratorTest {

    private Question question;
    private QuestionComponent basic;

    @BeforeEach
    void setUp() {
        question = new QuestionDirector(new QuestionBuilder()).buildSampleQuestion();
        basic = new BasicQuestion(question);
    }

    // ---- BasicQuestion ----

    @Test
    @DisplayName("la pregunta básica vale 1.0 y expone enunciado y justificación originales")
    void basica() {
        assertSame(question, basic.getQuestion());
        assertEquals(1.0, basic.getMaxScore());
        assertEquals(question.getStatement(), basic.describe());
        assertEquals(question.getJustification(), basic.getExplanation());
    }

    // ---- HighDifficultyQuestion ----

    @Test
    @DisplayName("alta dificultad multiplica el puntaje por 1.5 por defecto")
    void altaDificultadPorDefecto() {
        QuestionComponent decorada = new HighDifficultyQuestion(basic);

        assertEquals(1.5, decorada.getMaxScore());
    }

    @Test
    @DisplayName("alta dificultad acepta un multiplicador propio")
    void altaDificultadMultiplicadorPropio() {
        assertEquals(3.0, new HighDifficultyQuestion(basic, 3.0).getMaxScore());
    }

    @Test
    @DisplayName("alta dificultad añade una marca a la descripción sin tocar el resto")
    void altaDificultadDescribe() {
        QuestionComponent decorada = new HighDifficultyQuestion(basic, 2.0);

        assertEquals(question.getStatement() + " [Alta dificultad x2.0]", decorada.describe());
        assertEquals(basic.getExplanation(), decorada.getExplanation());
        assertSame(question, decorada.getQuestion());
    }

    @Test
    @DisplayName("alta dificultad no acepta un multiplicador menor o igual a cero")
    void altaDificultadMultiplicadorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new HighDifficultyQuestion(basic, 0));
        assertThrows(IllegalArgumentException.class, () -> new HighDifficultyQuestion(basic, -1));
    }

    // ---- FeedbackQuestion ----

    @Test
    @DisplayName("la retroalimentación añade la explicación técnica a la justificación original")
    void retroalimentacionExplica() {
        QuestionComponent decorada = new FeedbackQuestion(basic, "  Observer desacopla al sujeto de sus observadores. ");

        assertEquals(question.getJustification() + " Explicación técnica: Observer desacopla al sujeto de sus observadores.",
                decorada.getExplanation());
        assertTrue(decorada.describe().endsWith("[Con explicación técnica]"));
    }

    @Test
    @DisplayName("la retroalimentación no cambia el puntaje")
    void retroalimentacionNoCambiaPuntaje() {
        assertEquals(1.0, new FeedbackQuestion(basic, "Explicación").getMaxScore());
    }

    @Test
    @DisplayName("la retroalimentación no acepta una explicación vacía")
    void retroalimentacionVacia() {
        assertThrows(IllegalArgumentException.class, () -> new FeedbackQuestion(basic, " "));
        assertThrows(IllegalArgumentException.class, () -> new FeedbackQuestion(basic, null));
    }

    // ---- QuestionDecorator ----

    @Test
    @DisplayName("un decorador necesita un componente que envolver")
    void decoradorSinComponente() {
        assertThrows(IllegalArgumentException.class, () -> new HighDifficultyQuestion(null));
        assertThrows(IllegalArgumentException.class, () -> new FeedbackQuestion(null, "x"));
    }

    @Test
    @DisplayName("los decoradores son componentes y se pueden envolver entre sí")
    void decoradoresSonComponentes() {
        assertInstanceOf(QuestionComponent.class, new HighDifficultyQuestion(basic));
        assertInstanceOf(QuestionDecorator.class, new FeedbackQuestion(basic, "x"));
    }

    @Test
    @DisplayName("los decoradores se apilan y suman sus efectos")
    void apilados() {
        QuestionComponent decorada = new FeedbackQuestion(new HighDifficultyQuestion(basic, 2.0), "Detalle");

        assertEquals(2.0, decorada.getMaxScore());
        assertTrue(decorada.describe().contains("[Alta dificultad x2.0]"));
        assertTrue(decorada.describe().endsWith("[Con explicación técnica]"));
        assertTrue(decorada.getExplanation().endsWith("Explicación técnica: Detalle"));
    }

    @Test
    @DisplayName("el orden en que se apilan los decoradores no cambia el puntaje ni la explicación")
    void ordenDeApilado() {
        QuestionComponent a = new FeedbackQuestion(new HighDifficultyQuestion(basic, 2.0), "D");
        QuestionComponent b = new HighDifficultyQuestion(new FeedbackQuestion(basic, "D"), 2.0);

        assertEquals(a.getMaxScore(), b.getMaxScore());
        assertEquals(a.getExplanation(), b.getExplanation());
    }

    @Test
    @DisplayName("dos multiplicadores apilados se multiplican entre sí")
    void multiplicadoresApilados() {
        QuestionComponent decorada = new HighDifficultyQuestion(new HighDifficultyQuestion(basic, 2.0), 1.5);

        assertEquals(3.0, decorada.getMaxScore());
    }

    @Test
    @DisplayName("decorar no modifica la pregunta básica original")
    void noModificaLaOriginal() {
        new HighDifficultyQuestion(new FeedbackQuestion(basic, "D"), 4.0);

        assertEquals(1.0, basic.getMaxScore());
        assertEquals(question.getJustification(), basic.getExplanation());
    }
}

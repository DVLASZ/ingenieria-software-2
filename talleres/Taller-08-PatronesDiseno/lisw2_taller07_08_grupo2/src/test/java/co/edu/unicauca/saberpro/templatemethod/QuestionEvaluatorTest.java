package co.edu.unicauca.saberpro.templatemethod;

import co.edu.unicauca.saberpro.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.builder.QuestionDirector;
import co.edu.unicauca.saberpro.domain.model.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Patrón Template Method: QuestionEvaluator y sus subclases")
class QuestionEvaluatorTest {

    private Question observer;

    @BeforeEach
    void setUp() {
        observer = new QuestionDirector(new QuestionBuilder()).buildSampleQuestion();
    }

    // ---- La plantilla ----

    /** Evaluador de prueba que anota en qué orden se ejecutan los pasos. */
    private static class EvaluadorEspia extends QuestionEvaluator {
        final List<String> pasos = new ArrayList<>();
        private final boolean formatoValido;

        EvaluadorEspia(boolean formatoValido) {
            this.formatoValido = formatoValido;
        }

        @Override
        protected String receiveAnswer(String rawAnswer) {
            pasos.add("recibir");
            return super.receiveAnswer(rawAnswer);
        }

        @Override
        protected Optional<String> validateFormat(String answer) {
            pasos.add("validar");
            return formatoValido ? Optional.empty() : Optional.of("formato malo");
        }

        @Override
        protected double calculateScore(String answer) {
            pasos.add("puntuar");
            return 0.5;
        }

        @Override
        protected String generateFeedback(String answer, double score) {
            pasos.add("retroalimentar");
            return "ok";
        }
    }

    @Test
    @DisplayName("la plantilla ejecuta los cuatro pasos en orden")
    void plantillaEjecutaLosPasosEnOrden() {
        EvaluadorEspia evaluador = new EvaluadorEspia(true);

        EvaluationResult resultado = evaluador.evaluate("x");

        assertEquals(List.of("recibir", "validar", "puntuar", "retroalimentar"), evaluador.pasos);
        assertTrue(resultado.valid());
        assertEquals(0.5, resultado.score());
        assertEquals("ok", resultado.feedback());
    }

    @Test
    @DisplayName("si el formato es inválido no se calcula puntaje ni retroalimentación")
    void formatoInvalidoCortaLaPlantilla() {
        EvaluadorEspia evaluador = new EvaluadorEspia(false);

        EvaluationResult resultado = evaluador.evaluate("x");

        assertEquals(List.of("recibir", "validar"), evaluador.pasos);
        assertEquals(EvaluationResult.invalid("formato malo"), resultado);
    }

    @Test
    @DisplayName("una respuesta nula se recibe como texto vacío")
    void respuestaNula() {
        EvaluationResult resultado = new MultipleChoiceEvaluator(observer).evaluate(null);

        assertFalse(resultado.valid());
    }

    @Test
    @DisplayName("el resultado inválido tiene puntaje cero y el motivo como retroalimentación")
    void resultadoInvalido() {
        EvaluationResult resultado = EvaluationResult.invalid("motivo");

        assertFalse(resultado.valid());
        assertEquals(0.0, resultado.score());
        assertEquals("motivo", resultado.feedback());
    }

    // ---- MultipleChoiceEvaluator ----

    @Test
    @DisplayName("selección múltiple: la letra correcta da puntaje completo y la justificación")
    void seleccionMultipleCorrecta() {
        EvaluationResult resultado = new MultipleChoiceEvaluator(observer).evaluate("B");

        assertTrue(resultado.valid());
        assertEquals(1.0, resultado.score());
        assertTrue(resultado.feedback().startsWith("¡Correcto!"));
        assertTrue(resultado.feedback().contains(observer.getJustification()));
    }

    @Test
    @DisplayName("selección múltiple: una letra incorrecta da cero e indica la respuesta correcta")
    void seleccionMultipleIncorrecta() {
        EvaluationResult resultado = new MultipleChoiceEvaluator(observer).evaluate("A");

        assertTrue(resultado.valid());
        assertEquals(0.0, resultado.score());
        assertTrue(resultado.feedback().contains("B) Observer"));
    }

    @Test
    @DisplayName("selección múltiple: acepta minúsculas y espacios")
    void seleccionMultipleNormaliza() {
        assertEquals(1.0, new MultipleChoiceEvaluator(observer).evaluate("  b ").score());
    }

    @Test
    @DisplayName("selección múltiple: rechaza una respuesta que no es una letra de opción")
    void seleccionMultipleFormatoInvalido() {
        MultipleChoiceEvaluator evaluador = new MultipleChoiceEvaluator(observer);

        assertFalse(evaluador.evaluate("E").valid());
        assertFalse(evaluador.evaluate("Observer").valid());
        assertFalse(evaluador.evaluate("").valid());
        assertTrue(evaluador.evaluate("Z").feedback().contains("letra de una opción"));
    }

    // ---- TrueFalseEvaluator ----

    @Test
    @DisplayName("verdadero/falso: acierta cuando la afirmación es verdadera")
    void verdaderoFalsoAciertaVerdadera() {
        TrueFalseEvaluator evaluador = new TrueFalseEvaluator(true, "Es un patrón creacional.");

        EvaluationResult resultado = evaluador.evaluate("V");

        assertEquals(1.0, resultado.score());
        assertTrue(resultado.feedback().contains("verdadera"));
        assertTrue(resultado.feedback().contains("Es un patrón creacional."));
    }

    @Test
    @DisplayName("verdadero/falso: acierta cuando la afirmación es falsa")
    void verdaderoFalsoAciertaFalsa() {
        TrueFalseEvaluator evaluador = new TrueFalseEvaluator(false, "No es estructural.");

        assertEquals(1.0, evaluador.evaluate("falso").score());
    }

    @Test
    @DisplayName("verdadero/falso: falla y explica cuando se contesta al revés")
    void verdaderoFalsoFalla() {
        TrueFalseEvaluator evaluador = new TrueFalseEvaluator(true, "Explicación.");

        EvaluationResult resultado = evaluador.evaluate("F");

        assertEquals(0.0, resultado.score());
        assertTrue(resultado.feedback().startsWith("Incorrecto."));
    }

    @Test
    @DisplayName("verdadero/falso: acepta varias formas de escribir la respuesta")
    void verdaderoFalsoSinoNimos() {
        TrueFalseEvaluator evaluador = new TrueFalseEvaluator(true, "E");

        for (String respuesta : List.of("v", "Verdadero", "TRUE", "t", " V ")) {
            assertEquals(1.0, evaluador.evaluate(respuesta).score(), respuesta);
        }
        for (String respuesta : List.of("f", "Falso", "FALSE")) {
            assertEquals(0.0, evaluador.evaluate(respuesta).score(), respuesta);
        }
    }

    @Test
    @DisplayName("verdadero/falso: rechaza una respuesta que no es verdadero ni falso")
    void verdaderoFalsoFormatoInvalido() {
        EvaluationResult resultado = new TrueFalseEvaluator(true, "E").evaluate("quizás");

        assertFalse(resultado.valid());
        assertTrue(resultado.feedback().contains("verdadero o falso"));
    }

    // ---- CaseStudyEvaluator ----

    private CaseStudyEvaluator estudioDeCaso() {
        return new CaseStudyEvaluator(List.of("Observer", "notifica", "suscriptores"));
    }

    private static final String RESPUESTA_COMPLETA =
            "El patrón Observer notifica a todos los suscriptores cuando cambia el estado del sujeto.";

    @Test
    @DisplayName("caso de estudio: una respuesta que cubre toda la rúbrica da puntaje completo")
    void casoDeEstudioCompleto() {
        EvaluationResult resultado = estudioDeCaso().evaluate(RESPUESTA_COMPLETA);

        assertEquals(1.0, resultado.score());
        assertTrue(resultado.feedback().startsWith("¡Excelente!"));
    }

    @Test
    @DisplayName("caso de estudio: el puntaje es la fracción de criterios cubiertos y se indica lo que falta")
    void casoDeEstudioParcial() {
        EvaluationResult resultado = estudioDeCaso()
                .evaluate("El patrón Observer sirve para organizar mejor las dependencias del sistema.");

        assertEquals(1.0 / 3.0, resultado.score(), 1e-9);
        assertTrue(resultado.feedback().contains("1 de 3"));
        assertTrue(resultado.feedback().contains("notifica, suscriptores"));
    }

    @Test
    @DisplayName("caso de estudio: una respuesta sin ningún criterio da cero")
    void casoDeEstudioSinCriterios() {
        EvaluationResult resultado = estudioDeCaso()
                .evaluate("Es un tema que no recuerdo bien porque no estudié lo suficiente para esto.");

        assertTrue(resultado.valid());
        assertEquals(0.0, resultado.score());
    }

    @Test
    @DisplayName("caso de estudio: ignora mayúsculas al buscar los criterios")
    void casoDeEstudioIgnoraMayusculas() {
        assertEquals(1.0, estudioDeCaso().evaluate(RESPUESTA_COMPLETA.toUpperCase()).score());
    }

    @Test
    @DisplayName("caso de estudio: rechaza una respuesta demasiado corta")
    void casoDeEstudioMuyCorta() {
        EvaluationResult resultado = estudioDeCaso().evaluate("Observer");

        assertFalse(resultado.valid());
        assertTrue(resultado.feedback().contains("al menos " + CaseStudyEvaluator.MIN_LENGTH));
    }

    @Test
    @DisplayName("caso de estudio: la rúbrica no puede estar vacía")
    void casoDeEstudioRubricaVacia() {
        assertThrows(IllegalArgumentException.class, () -> new CaseStudyEvaluator(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new CaseStudyEvaluator(null));
    }
}

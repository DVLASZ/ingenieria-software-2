package co.edu.unicauca.saberpro;

import co.edu.unicauca.saberpro.domain.model.Competence;
import co.edu.unicauca.saberpro.domain.model.Difficulty;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.decorator.BasicQuestion;
import co.edu.unicauca.saberpro.decorator.FeedbackQuestion;
import co.edu.unicauca.saberpro.decorator.HighDifficultyQuestion;
import co.edu.unicauca.saberpro.decorator.QuestionComponent;
import co.edu.unicauca.saberpro.facade.ImportSummary;
import co.edu.unicauca.saberpro.facade.QuestionBankException;
import co.edu.unicauca.saberpro.facade.QuestionBankFacade;
import co.edu.unicauca.saberpro.state.QuestionLifecycle;
import co.edu.unicauca.saberpro.templatemethod.CaseStudyEvaluator;
import co.edu.unicauca.saberpro.templatemethod.EvaluationResult;
import co.edu.unicauca.saberpro.templatemethod.MultipleChoiceEvaluator;
import co.edu.unicauca.saberpro.templatemethod.TrueFalseEvaluator;

import java.util.List;
import java.util.Locale;

/**
 * Demostración del taller. La Parte 1 (Builder, Adapter) solo habla con {@link QuestionBankFacade}: no conoce el
 * repositorio, el validador ni los patrones internos. La Parte 2 recorre State, Template Method y Decorator.
 */
public class ApplicationMain {

    public static void main(String[] args) {
        QuestionBankFacade bank = new QuestionBankFacade();

        System.out.println("=== Facade: crear una pregunta (usa Builder por dentro) ===");
        Question created = bank.createQuestion(
                "Una aplicación debe actualizar varias ventanas cada vez que cambian los datos del modelo.",
                "¿Qué patrón notifica a varios objetos cuando cambia el estado de otro?",
                List.of("Factory Method", "Observer", "Singleton", "Adapter"), 1,
                "Observer define una dependencia uno a muchos entre objetos.",
                Competence.CRITICAL_READING, "Patrones de diseño", "Observer", Difficulty.INTERMEDIATE);
        System.out.println(created + " -> respuesta correcta: " + created.getCorrectOption().text());

        System.out.println("\n=== Facade: una pregunta inválida se rechaza con un solo tipo de error ===");
        try {
            bank.createQuestion("Contexto", "Enunciado sin signo de interrogación", List.of("a", "b", "c", "d"), 0,
                    "J", Competence.ENGLISH, "Tema", "Subtema", Difficulty.BASIC);
        } catch (QuestionBankException e) {
            System.out.println("Rechazada: " + e.getMessage());
        }

        System.out.println("\n=== Facade: importar desde el servicio externo (usa Adapter por dentro) ===");
        ImportSummary summary = bank.importExternalQuestions();
        System.out.println("Importadas: " + summary.imported());
        summary.rejected().forEach(motivo -> System.out.println("  " + motivo));

        System.out.println("\n=== Facade: banco de preguntas (" + bank.listQuestions().size() + ") ===");
        bank.listQuestions().forEach(question -> System.out.println("  " + question));

        Question observer = created;
        demoState(observer);
        demoTemplateMethod(observer);
        demoDecorator(observer);
    }

    private static void demoState(Question question) {
        System.out.println("\n=== State: ciclo de vida de la pregunta ===");
        QuestionLifecycle lifecycle = new QuestionLifecycle(question);
        System.out.println("Estado inicial: " + lifecycle.getState().name());
        try {
            lifecycle.approve();
        } catch (IllegalStateException e) {
            System.out.println("Rechazado: " + e.getMessage());
        }
        lifecycle.submitForReview();
        lifecycle.startReview();
        lifecycle.requestChanges();
        lifecycle.submitForReview();
        lifecycle.startReview();
        lifecycle.approve();
        System.out.println("Recorrido: " + String.join(" -> ", lifecycle.getHistory()));
        System.out.println("¿Publicable? " + lifecycle.isPublishable());
    }

    private static void demoTemplateMethod(Question question) {
        System.out.println("\n=== Template Method: evaluación de respuestas ===");
        show("Selección múltiple, responde B", new MultipleChoiceEvaluator(question).evaluate("B"));
        show("Selección múltiple, responde A", new MultipleChoiceEvaluator(question).evaluate("A"));
        show("Selección múltiple, responde 7", new MultipleChoiceEvaluator(question).evaluate("7"));
        TrueFalseEvaluator trueFalse = new TrueFalseEvaluator(true, "Builder es un patrón creacional.");
        show("Verdadero/falso, responde V", trueFalse.evaluate("V"));
        show("Verdadero/falso, responde F", trueFalse.evaluate("F"));
        CaseStudyEvaluator caseStudy = new CaseStudyEvaluator(List.of("Observer", "notifica", "suscriptores"));
        show("Caso de estudio, respuesta parcial",
                caseStudy.evaluate("El patrón Observer sirve para organizar las dependencias del sistema."));
    }

    private static void show(String title, EvaluationResult result) {
        System.out.printf(Locale.ROOT, "  %s -> puntaje %.2f | %s%n", title, result.score(), result.feedback());
    }

    private static void demoDecorator(Question question) {
        System.out.println("\n=== Decorator: características dinámicas ===");
        QuestionComponent basic = new BasicQuestion(question);
        QuestionComponent decorated = new FeedbackQuestion(new HighDifficultyQuestion(basic, 2.0),
                "Observer desacopla al sujeto de sus observadores mediante una interfaz común.");
        System.out.printf(Locale.ROOT, "Básica:    %s (vale %.1f)%n", basic.describe(), basic.getMaxScore());
        System.out.printf(Locale.ROOT, "Decorada:  %s (vale %.1f)%n", decorated.describe(), decorated.getMaxScore());
        System.out.println("Explicación: " + decorated.getExplanation());
        double earned = new MultipleChoiceEvaluator(question).evaluate("B").score() * decorated.getMaxScore();
        System.out.printf(Locale.ROOT, "Puntos por acertar la decorada: %.1f%n", earned);
    }
}

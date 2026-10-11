package co.edu.unicauca.saberpro;

import co.edu.unicauca.saberpro.domain.model.Competence;
import co.edu.unicauca.saberpro.domain.model.Difficulty;
import co.edu.unicauca.saberpro.domain.model.Question;
import co.edu.unicauca.saberpro.facade.ImportSummary;
import co.edu.unicauca.saberpro.facade.QuestionBankException;
import co.edu.unicauca.saberpro.facade.QuestionBankFacade;

import java.util.List;

/**
 * Demostración del taller. Por ahora recorre la Parte 1 (Builder, Adapter y Facade) y solo habla con
 * {@link QuestionBankFacade}: no conoce el repositorio, el validador ni los patrones internos.
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
    }
}

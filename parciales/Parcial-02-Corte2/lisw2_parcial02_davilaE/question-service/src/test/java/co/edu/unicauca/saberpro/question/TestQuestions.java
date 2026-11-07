package co.edu.unicauca.saberpro.question;

import co.edu.unicauca.saberpro.domain.builder.QuestionBuilder;
import co.edu.unicauca.saberpro.domain.model.Question;

/** preguntas de ejemplo para los tests */
public final class TestQuestions {

    public static final String VALID_JSON = """
            {
              "statement": "¿Qué patrón separa el núcleo de los módulos?",
              "justification": "El microkernel deja el núcleo mínimo y carga plugins",
              "options": [
                {"text": "Microkernel", "correct": true},
                {"text": "Monolito", "correct": false},
                {"text": "Singleton", "correct": false},
                {"text": "Facade", "correct": false}
              ]
            }
            """;

    private TestQuestions() {
    }

    public static Question draft() {
        return new QuestionBuilder()
                .statement("¿Qué patrón separa el núcleo de los módulos?")
                .justification("El microkernel deja el núcleo mínimo y carga plugins")
                .addOption("Microkernel", true)
                .addOption("Monolito", false)
                .addOption("Singleton", false)
                .addOption("Facade", false)
                .build();
    }
}

package co.edu.unicauca.saberpro.builder;

import co.edu.unicauca.saberpro.domain.model.Competence;
import co.edu.unicauca.saberpro.domain.model.Difficulty;
import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.List;

/**
 * Director del patrón Builder: conoce el orden de los pasos y arma preguntas completas usando cualquier
 * {@link IQuestionBuilder}.
 */
public class QuestionDirector {

    private final IQuestionBuilder builder;

    public QuestionDirector(IQuestionBuilder builder) {
        this.builder = builder;
    }

    /**
     * Arma una pregunta de selección múltiple en el orden estándar de los pasos.
     *
     * @param correctIndex posición (0 a 3) de la opción correcta dentro de {@code optionTexts}
     */
    public Question buildMultipleChoice(String context, String statement, List<String> optionTexts, int correctIndex,
                                        String justification, Competence competence, String topic, String subtopic,
                                        Difficulty difficulty) {
        builder.reset().setContext(context).setStatement(statement);
        for (int i = 0; i < optionTexts.size(); i++) {
            builder.addOption(optionTexts.get(i), i == correctIndex);
        }
        return builder.setJustification(justification)
                .setClassification(competence, topic, subtopic, difficulty)
                .build();
    }

    /** Pregunta de ejemplo lista para usar en demostraciones y pruebas. */
    public Question buildSampleQuestion() {
        return buildMultipleChoice(
                "Una aplicación debe actualizar varias ventanas cada vez que cambian los datos del modelo.",
                "¿Qué patrón notifica a varios objetos cuando cambia el estado de otro?",
                List.of("Factory Method", "Observer", "Singleton", "Adapter"),
                1,
                "Observer define una dependencia uno a muchos entre objetos: cuando uno cambia, todos los suscritos se actualizan.",
                Competence.CRITICAL_READING, "Patrones de diseño", "Observer", Difficulty.INTERMEDIATE);
    }
}

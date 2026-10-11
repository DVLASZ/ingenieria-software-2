package co.edu.unicauca.saberpro.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * Pregunta de selección múltiple con única respuesta del banco de preguntas Saber Pro.
 *
 * <p>Es un objeto complejo con muchas partes, por eso se construye con el patrón Builder
 * ({@code QuestionBuilder}). Una vez creada no cambia.
 */
public final class Question {

    private final String id;
    private final Context context;
    private final String statement;
    private final List<Option> options;
    private final String justification;
    private final Classification classification;

    public Question(Context context, String statement, List<Option> options, String justification,
                    Classification classification) {
        this.id = UUID.randomUUID().toString();
        this.context = context;
        this.statement = statement;
        this.options = List.copyOf(options);
        this.justification = justification;
        this.classification = classification;
    }

    public String getId() {
        return id;
    }

    public Context getContext() {
        return context;
    }

    public String getStatement() {
        return statement;
    }

    public List<Option> getOptions() {
        return options;
    }

    public String getJustification() {
        return justification;
    }

    public Classification getClassification() {
        return classification;
    }

    /** La opción correcta; el constructor garantiza que existe una sola. */
    public Option getCorrectOption() {
        return options.stream().filter(Option::correct).findFirst().orElseThrow();
    }

    @Override
    public String toString() {
        return "Question{" + statement + " [" + classification.competence() + ", " + classification.difficulty() + "]}";
    }
}

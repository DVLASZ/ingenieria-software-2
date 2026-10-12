package co.edu.unicauca.saberpro.decorator;

/** Decorador que adjunta a la pregunta una explicación técnica detallada. */
public class FeedbackQuestion extends QuestionDecorator {

    private final String technicalExplanation;

    /** @param technicalExplanation explicación detallada; no puede estar vacía */
    public FeedbackQuestion(QuestionComponent wrapped, String technicalExplanation) {
        super(wrapped);
        if (technicalExplanation == null || technicalExplanation.isBlank()) {
            throw new IllegalArgumentException("La explicación técnica no puede estar vacía");
        }
        this.technicalExplanation = technicalExplanation.strip();
    }

    @Override
    public String describe() {
        return super.describe() + " [Con explicación técnica]";
    }

    @Override
    public String getExplanation() {
        return super.getExplanation() + " Explicación técnica: " + technicalExplanation;
    }
}

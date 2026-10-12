package co.edu.unicauca.saberpro.decorator;

import java.util.Locale;

/** Decorador que marca la pregunta como de alta dificultad y multiplica su puntaje máximo. */
public class HighDifficultyQuestion extends QuestionDecorator {

    /** Multiplicador por defecto. */
    public static final double DEFAULT_MULTIPLIER = 1.5;

    private final double multiplier;

    public HighDifficultyQuestion(QuestionComponent wrapped) {
        this(wrapped, DEFAULT_MULTIPLIER);
    }

    /** @param multiplier factor por el que se multiplica el puntaje; debe ser mayor que 0 */
    public HighDifficultyQuestion(QuestionComponent wrapped, double multiplier) {
        super(wrapped);
        if (multiplier <= 0) {
            throw new IllegalArgumentException("El multiplicador debe ser mayor que 0");
        }
        this.multiplier = multiplier;
    }

    @Override
    public double getMaxScore() {
        return super.getMaxScore() * multiplier;
    }

    @Override
    public String describe() {
        return super.describe() + String.format(Locale.ROOT, " [Alta dificultad x%.1f]", multiplier);
    }
}

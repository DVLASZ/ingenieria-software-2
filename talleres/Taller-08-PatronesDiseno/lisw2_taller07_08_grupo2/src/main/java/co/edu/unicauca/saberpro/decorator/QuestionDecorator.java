package co.edu.unicauca.saberpro.decorator;

import co.edu.unicauca.saberpro.domain.model.Question;

/**
 * Decorador base: envuelve otro {@link QuestionComponent} y le delega todo. Los decoradores concretos sobrescriben solo
 * lo que modifican, y se pueden apilar en cualquier orden.
 */
public abstract class QuestionDecorator implements QuestionComponent {

    protected final QuestionComponent wrapped;

    protected QuestionDecorator(QuestionComponent wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("El decorador necesita una pregunta que envolver");
        }
        this.wrapped = wrapped;
    }

    @Override
    public Question getQuestion() {
        return wrapped.getQuestion();
    }

    @Override
    public double getMaxScore() {
        return wrapped.getMaxScore();
    }

    @Override
    public String describe() {
        return wrapped.describe();
    }

    @Override
    public String getExplanation() {
        return wrapped.getExplanation();
    }
}

package co.edu.unicauca.saberpro.state;

/**
 * Pendiente de revisión: la pregunta ya fue enviada. El autor no puede modificarla; solo avanza cuando un revisor
 * inicia la revisión.
 */
public class PendingReviewState implements IQuestionState {

    @Override
    public String name() {
        return "Pendiente de revisión";
    }

    @Override
    public void startReview(QuestionLifecycle lifecycle) {
        lifecycle.changeState(new ReviewState());
    }
}

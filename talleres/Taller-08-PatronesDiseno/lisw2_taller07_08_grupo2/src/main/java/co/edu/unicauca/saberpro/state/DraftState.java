package co.edu.unicauca.saberpro.state;

import co.edu.unicauca.saberpro.domain.model.Question;

/** Borrador: estado inicial. Permite la edición completa y solo puede pasar a «Pendiente de revisión». */
public class DraftState implements IQuestionState {

    @Override
    public String name() {
        return "Borrador";
    }

    @Override
    public void edit(QuestionLifecycle lifecycle, Question edited) {
        lifecycle.replaceQuestion(edited);
    }

    @Override
    public void submitForReview(QuestionLifecycle lifecycle) {
        lifecycle.changeState(new PendingReviewState());
    }
}

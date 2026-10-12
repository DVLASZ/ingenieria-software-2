package co.edu.unicauca.saberpro.state;

/** En revisión (por pares): el revisor puede aprobar la pregunta o devolverla al autor con correcciones. */
public class ReviewState implements IQuestionState {

    @Override
    public String name() {
        return "En revisión";
    }

    @Override
    public void approve(QuestionLifecycle lifecycle) {
        lifecycle.changeState(new ApprovedState());
    }

    @Override
    public void requestChanges(QuestionLifecycle lifecycle) {
        lifecycle.changeState(new DraftState());
    }
}

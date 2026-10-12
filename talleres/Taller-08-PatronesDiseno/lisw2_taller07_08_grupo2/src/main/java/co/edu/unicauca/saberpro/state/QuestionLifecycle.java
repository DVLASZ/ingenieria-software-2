package co.edu.unicauca.saberpro.state;

import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * Contexto del patrón State: una pregunta junto con su estado actual.
 *
 * <p>Toda operación se delega al estado; es él quien decide si la acepta y a qué estado pasa. Empieza siempre en
 * {@link DraftState}.
 */
public class QuestionLifecycle {

    private Question question;
    private IQuestionState state;
    private final List<String> history = new ArrayList<>();

    public QuestionLifecycle(Question question) {
        this.question = question;
        changeState(new DraftState());
    }

    public void edit(Question edited) {
        state.edit(this, edited);
    }

    public void submitForReview() {
        state.submitForReview(this);
    }

    public void startReview() {
        state.startReview(this);
    }

    public void approve() {
        state.approve(this);
    }

    public void requestChanges() {
        state.requestChanges(this);
    }

    public boolean isPublishable() {
        return state.isPublishable();
    }

    public Question getQuestion() {
        return question;
    }

    public IQuestionState getState() {
        return state;
    }

    /** Nombres de los estados por los que ha pasado la pregunta, en orden, incluido el actual. */
    public List<String> getHistory() {
        return List.copyOf(history);
    }

    void changeState(IQuestionState next) {
        this.state = next;
        history.add(next.name());
    }

    void replaceQuestion(Question edited) {
        this.question = edited;
    }
}

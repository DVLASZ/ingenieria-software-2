package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.model.state.IQuestionState;

import java.util.List;

/** pregunta de selección múltiple. Java puro, sin anotaciones de persistencia */
public class Question {

    private final Long id;
    private final String statement;
    private final String justification;
    private final List<AnswerOption> options;
    private IQuestionState state;

    public Question(Long id, String statement, String justification, List<AnswerOption> options, IQuestionState state) {
        this.id = id;
        this.statement = statement;
        this.justification = justification;
        this.options = List.copyOf(options);
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public String getStatement() {
        return statement;
    }

    public String getJustification() {
        return justification;
    }

    public List<AnswerOption> getOptions() {
        return options;
    }

    public QuestionStatus getStatus() {
        return state.status();
    }

    /** pasa al estado siguiente; el estado actual decide si se puede */
    public void submitForReview() {
        this.state = state.submitForReview();
    }

    /** lanza InvalidTransitionException si no se puede enviar, sin cambiar nada */
    public void ensureCanSubmit() {
        state.submitForReview();
    }

    public Question withStatus(QuestionStatus newStatus) {
        return new Question(id, statement, justification, options, newStatus.newState());
    }
}

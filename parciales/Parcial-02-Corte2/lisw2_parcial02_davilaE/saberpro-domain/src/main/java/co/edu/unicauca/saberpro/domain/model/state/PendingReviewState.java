package co.edu.unicauca.saberpro.domain.model.state;

import co.edu.unicauca.saberpro.domain.exception.InvalidTransitionException;
import co.edu.unicauca.saberpro.domain.model.QuestionStatus;

public class PendingReviewState implements IQuestionState {

    @Override
    public QuestionStatus status() {
        return QuestionStatus.PENDING_REVIEW;
    }

    @Override
    public IQuestionState submitForReview() {
        throw new InvalidTransitionException("La pregunta ya está pendiente de revisión, solo se puede enviar un borrador");
    }
}

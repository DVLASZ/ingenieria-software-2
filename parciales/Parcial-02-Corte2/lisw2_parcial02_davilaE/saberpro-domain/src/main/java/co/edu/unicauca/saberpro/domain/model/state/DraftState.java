package co.edu.unicauca.saberpro.domain.model.state;

import co.edu.unicauca.saberpro.domain.model.QuestionStatus;

public class DraftState implements IQuestionState {

    @Override
    public QuestionStatus status() {
        return QuestionStatus.DRAFT;
    }

    @Override
    public IQuestionState submitForReview() {
        return new PendingReviewState();
    }
}

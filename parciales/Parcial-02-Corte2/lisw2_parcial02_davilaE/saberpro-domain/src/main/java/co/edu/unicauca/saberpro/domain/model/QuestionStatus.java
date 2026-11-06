package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.model.state.DraftState;
import co.edu.unicauca.saberpro.domain.model.state.IQuestionState;
import co.edu.unicauca.saberpro.domain.model.state.PendingReviewState;

import java.util.function.Supplier;

/** nombre del estado (es lo que se guarda y viaja en los mensajes) y fábrica de su objeto State */
public enum QuestionStatus {
    DRAFT(DraftState::new),
    PENDING_REVIEW(PendingReviewState::new);

    private final Supplier<IQuestionState> factory;

    QuestionStatus(Supplier<IQuestionState> factory) {
        this.factory = factory;
    }

    public IQuestionState newState() {
        return factory.get();
    }
}

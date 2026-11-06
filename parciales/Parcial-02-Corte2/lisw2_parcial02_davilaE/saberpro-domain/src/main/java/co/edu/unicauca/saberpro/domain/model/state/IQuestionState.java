package co.edu.unicauca.saberpro.domain.model.state;

import co.edu.unicauca.saberpro.domain.model.QuestionStatus;

/** patrón State: cada estado decide a cuál pasa o si no puede pasar, sin if ni switch */
public interface IQuestionState {

    QuestionStatus status();

    /** devuelve el estado siguiente o lanza InvalidTransitionException */
    IQuestionState submitForReview();
}

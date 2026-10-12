package co.edu.unicauca.saberpro.state;

import co.edu.unicauca.saberpro.domain.model.Question;

/**
 * Estado del ciclo de vida de una pregunta (patrón State).
 *
 * <p>Cada operación está prohibida por defecto y lanza {@link IllegalStateException}; cada estado concreto sobrescribe
 * únicamente las operaciones que permite. Así no hay {@code if/else} ni {@code switch} sobre el estado en ninguna parte.
 */
public interface IQuestionState {

    /** Nombre legible del estado, por ejemplo «Borrador». */
    String name();

    /** Reemplaza el contenido de la pregunta por una versión editada. */
    default void edit(QuestionLifecycle lifecycle, Question edited) {
        reject("editar la pregunta");
    }

    /** El autor envía la pregunta a revisión. */
    default void submitForReview(QuestionLifecycle lifecycle) {
        reject("enviar a revisión");
    }

    /** Un revisor toma la pregunta y comienza la revisión por pares. */
    default void startReview(QuestionLifecycle lifecycle) {
        reject("iniciar la revisión");
    }

    /** El revisor aprueba la pregunta. */
    default void approve(QuestionLifecycle lifecycle) {
        reject("aprobar");
    }

    /** El revisor devuelve la pregunta al autor para que la corrija. */
    default void requestChanges(QuestionLifecycle lifecycle) {
        reject("solicitar correcciones");
    }

    /** Indica si la pregunta puede usarse en simulacros. */
    default boolean isPublishable() {
        return false;
    }

    private void reject(String action) {
        throw new IllegalStateException("Acción no permitida en estado " + name() + ": " + action);
    }
}

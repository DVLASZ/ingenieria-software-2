package co.edu.unicauca.saberpro.decorator;

import co.edu.unicauca.saberpro.domain.model.Question;

/**
 * Componente del patrón Decorator: lo que se puede pedir a una pregunta, con o sin características añadidas.
 * {@link BasicQuestion} es el componente concreto y los decoradores lo envuelven.
 */
public interface QuestionComponent {

    /** La pregunta original, sin modificar. */
    Question getQuestion();

    /** Puntaje máximo que vale la pregunta. */
    double getMaxScore();

    /** Descripción corta de la pregunta con las características añadidas. */
    String describe();

    /** Explicación que se muestra tras responder. */
    String getExplanation();
}

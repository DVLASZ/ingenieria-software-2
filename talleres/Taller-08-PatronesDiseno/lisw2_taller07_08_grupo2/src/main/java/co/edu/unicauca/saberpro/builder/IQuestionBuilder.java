package co.edu.unicauca.saberpro.builder;

import co.edu.unicauca.saberpro.domain.model.Competence;
import co.edu.unicauca.saberpro.domain.model.Difficulty;
import co.edu.unicauca.saberpro.domain.model.Question;

/**
 * Patrón Builder (creacional): contrato para construir una {@link Question} paso a paso.
 *
 * <p>Los pasos se pueden llamar en cualquier orden. {@link #build()} comprueba que la pregunta esté completa y
 * que tenga exactamente 4 opciones y 1 respuesta correcta.
 */
public interface IQuestionBuilder {

    /** Paso 1: el contexto que enmarca la pregunta. */
    IQuestionBuilder setContext(String text);

    /** Paso 2: la pregunta directa (enunciado). */
    IQuestionBuilder setStatement(String statement);

    /** Paso 3: agrega una opción; las letras A, B, C, D se asignan en el orden en que se agregan. */
    IQuestionBuilder addOption(String text, boolean correct);

    /** Paso 4: la justificación de la respuesta correcta. */
    IQuestionBuilder setJustification(String justification);

    /** Paso 5: la clasificación dentro del banco. */
    IQuestionBuilder setClassification(Competence competence, String topic, String subtopic, Difficulty difficulty);

    /**
     * Entrega la pregunta construida y deja el builder listo para una nueva.
     *
     * @throws InvalidQuestionException si falta alguna parte o no hay exactamente 4 opciones y 1 correcta
     */
    Question build();

    /** Descarta lo acumulado hasta ahora. */
    IQuestionBuilder reset();
}

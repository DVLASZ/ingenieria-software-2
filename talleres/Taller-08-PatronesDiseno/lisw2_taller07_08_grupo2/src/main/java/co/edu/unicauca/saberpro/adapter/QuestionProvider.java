package co.edu.unicauca.saberpro.adapter;

import co.edu.unicauca.saberpro.domain.model.Question;

import java.util.List;

/**
 * Interfaz objetivo (Target) del patrón Adapter: lo único que el banco de preguntas sabe pedirle a cualquier
 * fuente de preguntas, sin importar de dónde vengan ni en qué formato.
 */
public interface QuestionProvider {

    /** Entrega las preguntas ya convertidas al modelo del banco. */
    List<Question> getQuestions();
}

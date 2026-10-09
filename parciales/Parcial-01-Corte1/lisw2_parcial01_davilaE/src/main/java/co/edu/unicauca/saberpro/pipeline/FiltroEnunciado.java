package co.edu.unicauca.saberpro.pipeline;

import co.edu.unicauca.saberpro.model.Pregunta;

public class FiltroEnunciado implements IFiltroPregunta {
    @Override
    public boolean procesar(Pregunta pregunta) {
        if (pregunta == null || pregunta.getEnunciado() == null) {
            return false;
        }
        return !pregunta.getEnunciado().trim().isEmpty();
    }
}

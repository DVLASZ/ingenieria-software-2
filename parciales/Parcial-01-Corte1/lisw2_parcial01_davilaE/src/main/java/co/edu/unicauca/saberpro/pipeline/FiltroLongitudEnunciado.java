package co.edu.unicauca.saberpro.pipeline;

import co.edu.unicauca.saberpro.model.Pregunta;

public class FiltroLongitudEnunciado implements IFiltroPregunta {
    @Override
    public boolean procesar(Pregunta pregunta) {
        if (pregunta == null) return false;
        // ERROR 1: Lógica invertida en la condición de longitud mínima
        // La regla exige que el enunciado tenga al menos 15 caracteres para ser válido.
        // CORREGIDO: ahora rechaza solo los de menos de 15 caracteres (>= pasó a <).
        if (pregunta.getEnunciado() == null || pregunta.getEnunciado().length() < 15) {
            return false;
        }
        return true;
    }
}

package co.edu.unicauca.saberpro.pipeline;

import co.edu.unicauca.saberpro.model.Pregunta;

public interface IFiltroPregunta {
    boolean procesar(Pregunta pregunta);
}

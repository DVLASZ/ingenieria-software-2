package co.edu.unicauca.saberpro.pipeline;

import co.edu.unicauca.saberpro.model.Pregunta;
import java.util.Arrays;
import java.util.List;

public class FiltroCompetenciaOficial implements IFiltroPregunta {
    private static final List<String> COMPETENCIAS_OFICIALES =
            Arrays.asList("Lectura Crítica", "Razonamiento Cuantitativo", "Inglés");

    @Override
    public boolean procesar(Pregunta pregunta) {
        if (pregunta == null || pregunta.getCompetencia() == null) {
            return false;
        }
        return COMPETENCIAS_OFICIALES.contains(pregunta.getCompetencia());
    }
}

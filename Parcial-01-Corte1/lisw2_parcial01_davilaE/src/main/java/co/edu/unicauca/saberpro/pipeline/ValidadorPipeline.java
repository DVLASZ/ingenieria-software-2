package co.edu.unicauca.saberpro.pipeline;

import co.edu.unicauca.saberpro.model.Pregunta;
import java.util.ArrayList;
import java.util.List;

public class ValidadorPipeline {
    private List<IFiltroPregunta> filtros = new ArrayList<>();

    public void addFiltro(IFiltroPregunta filtro) {
        filtros.add(filtro);
    }

    public boolean ejecutar(Pregunta pregunta) {
        for (IFiltroPregunta filtro : filtros) {
            if (!filtro.procesar(pregunta)) {
                System.out.println("Validación falló en: " + filtro.getClass().getSimpleName());
                return false;
            }
        }
        return true;
    }
}

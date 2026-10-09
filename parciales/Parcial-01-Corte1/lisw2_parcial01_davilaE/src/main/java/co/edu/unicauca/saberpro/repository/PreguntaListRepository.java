package co.edu.unicauca.saberpro.repository;

import co.edu.unicauca.saberpro.model.Pregunta;
import java.util.ArrayList;
import java.util.List;

public class PreguntaListRepository implements IPreguntaRepository {
    private List<Pregunta> bancoPreguntas = new ArrayList<>();

    @Override
    public boolean guardar(Pregunta pregunta) {
        // ERROR 2: Se creaba una lista nueva aquí y la pregunta se perdía, nunca llegaba al banco.
        // CORREGIDO: borré esa lista local para usar la de la clase.
        return bancoPreguntas.add(pregunta);
    }

    @Override
    public List<Pregunta> listar() {
        return bancoPreguntas;
    }

    @Override
    public Pregunta buscarPorId(int id) {
        return bancoPreguntas.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }
}

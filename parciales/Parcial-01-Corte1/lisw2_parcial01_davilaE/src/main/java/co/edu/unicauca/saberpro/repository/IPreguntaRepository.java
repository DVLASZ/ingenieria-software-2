package co.edu.unicauca.saberpro.repository;

import co.edu.unicauca.saberpro.model.Pregunta;
import java.util.List;

public interface IPreguntaRepository {
    boolean guardar(Pregunta pregunta);
    List<Pregunta> listar();
    Pregunta buscarPorId(int id);
}

package co.edu.unicauca.saberpro.service;

import co.edu.unicauca.saberpro.model.Pregunta;
import java.util.List;

public interface IPreguntaService {
    boolean registrarPregunta(Pregunta pregunta);
    List<Pregunta> obtenerTodas();
}

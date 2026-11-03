package co.edu.unicauca.saberpro.domain.repository;

import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import java.util.List;
import java.util.Optional;

/** Contrato para guardar y consultar preguntas. No dice cómo ni dónde se guardan. */
public interface PreguntaRepository {

    /** Guarda la pregunta; si es nueva le asigna su identidad. */
    Pregunta guardar(Pregunta pregunta);

    Optional<Pregunta> buscarPorId(Long id);

    List<Pregunta> buscarPorAutor(String autorId);

    List<Pregunta> buscarPorEstado(EstadoPregunta estado);
}

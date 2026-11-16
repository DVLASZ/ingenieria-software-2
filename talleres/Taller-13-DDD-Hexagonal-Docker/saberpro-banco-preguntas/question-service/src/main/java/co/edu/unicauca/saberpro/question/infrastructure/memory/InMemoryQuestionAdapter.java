package co.edu.unicauca.saberpro.question.infrastructure.memory;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryQuestionAdapter implements PreguntaRepositoryPort {

    private final Map<String, Pregunta> almacen = new HashMap<>();

    @Override
    public synchronized Pregunta save(Pregunta pregunta) {
        almacen.put(pregunta.id(), copiar(pregunta));
        return pregunta;
    }

    @Override
    public synchronized Optional<Pregunta> findById(String id) {
        return Optional.ofNullable(almacen.get(id)).map(this::copiar);
    }

    @Override
    public synchronized List<Pregunta> findAll() {
        List<Pregunta> todas = new ArrayList<>();
        almacen.values().forEach(p -> todas.add(copiar(p)));
        return todas;
    }

    // se guarda y se entrega una copia para no compartir el objeto con quien llama, como pasa con JPA
    private Pregunta copiar(Pregunta p) {
        return Pregunta.reconstruir(p.id(), p.enunciado(), p.justificacion(), p.opciones(), p.estado());
    }
}

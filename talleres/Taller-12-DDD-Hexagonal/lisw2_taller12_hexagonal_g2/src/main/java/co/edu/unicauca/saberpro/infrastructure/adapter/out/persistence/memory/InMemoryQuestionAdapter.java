package co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.memory;

import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import java.util.HashMap;
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

    // se guarda y se entrega una copia para no compartir el objeto con quien llama, como pasa con JPA
    private Pregunta copiar(Pregunta p) {
        return Pregunta.reconstruir(p.id(), p.enunciado(), p.justificacion(), p.opciones(), p.estado());
    }
}

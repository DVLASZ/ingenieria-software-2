package co.edu.unicauca.saberpro.domain.repository;

import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.model.Revision;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implementaciones en memoria de los contratos, solo para probar el dominio sin base de datos. */
final class RepositoriosEnMemoria {

    private RepositoriosEnMemoria() {
    }

    static class Preguntas implements PreguntaRepository {
        private final Map<Long, Pregunta> guardadas = new LinkedHashMap<>();
        private long siguiente = 1;

        @Override
        public Pregunta guardar(Pregunta pregunta) {
            if (pregunta.id() == null) {
                pregunta.asignarIdentidad(siguiente++);
            }
            guardadas.put(pregunta.id(), pregunta);
            return pregunta;
        }

        @Override
        public Optional<Pregunta> buscarPorId(Long id) {
            return Optional.ofNullable(guardadas.get(id));
        }

        @Override
        public List<Pregunta> buscarPorAutor(String autorId) {
            return guardadas.values().stream().filter(p -> autorId.equals(p.autorId())).toList();
        }

        @Override
        public List<Pregunta> buscarPorEstado(EstadoPregunta estado) {
            return new ArrayList<>(guardadas.values().stream().filter(p -> p.estado() == estado).toList());
        }
    }

    static class Revisiones implements RevisionRepository {
        private final Map<Long, Revision> guardadas = new LinkedHashMap<>();
        private long siguiente = 1;

        @Override
        public Revision guardar(Revision revision) {
            if (revision.id() == null) {
                revision.asignarIdentidad(siguiente++);
            }
            guardadas.put(revision.id(), revision);
            return revision;
        }

        @Override
        public Optional<Revision> buscarPorId(Long id) {
            return Optional.ofNullable(guardadas.get(id));
        }

        @Override
        public Optional<Revision> buscarPorPregunta(Long preguntaId) {
            return guardadas.values().stream().filter(r -> r.preguntaId().equals(preguntaId)).findFirst();
        }
    }
}

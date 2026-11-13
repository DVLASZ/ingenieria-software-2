package co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa;

import co.edu.unicauca.saberpro.domain.model.Enunciado;
import co.edu.unicauca.saberpro.domain.model.Justificacion;
import co.edu.unicauca.saberpro.domain.model.OpcionRespuesta;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import java.util.Optional;

public class JpaQuestionAdapter implements PreguntaRepositoryPort {

    private final SpringDataPreguntaRepository repositorio;

    public JpaQuestionAdapter(SpringDataPreguntaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Pregunta save(Pregunta pregunta) {
        return aDominio(repositorio.save(aEntidad(pregunta)));
    }

    @Override
    public Optional<Pregunta> findById(String id) {
        return repositorio.findById(id).map(this::aDominio);
    }

    private PreguntaJpaEntity aEntidad(Pregunta p) {
        return new PreguntaJpaEntity(p.id(), p.enunciado().texto(), p.justificacion().texto(), p.estado(),
                p.opciones().stream().map(o -> new OpcionEmbeddable(o.texto(), o.esCorrecta())).toList());
    }

    private Pregunta aDominio(PreguntaJpaEntity e) {
        return Pregunta.reconstruir(e.getId(), new Enunciado(e.getEnunciado()), new Justificacion(e.getJustificacion()),
                e.getOpciones().stream().map(o -> new OpcionRespuesta(o.getTexto(), o.isCorrecta())).toList(),
                e.getEstado());
    }
}

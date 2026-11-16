package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import co.edu.unicauca.saberpro.question.domain.model.Enunciado;
import co.edu.unicauca.saberpro.question.domain.model.Justificacion;
import co.edu.unicauca.saberpro.question.domain.model.OpcionRespuesta;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;

/** Traduce entre el agregado Pregunta y los registros JPA, en los dos sentidos. */
public class QuestionDomainMapper {

    public QuestionRecord toRecord(Pregunta p) {
        return new QuestionRecord(p.id(), p.enunciado().texto(), p.justificacion().texto(), p.estado(),
                p.opciones().stream().map(o -> new AnswerOptionRecord(o.texto(), o.esCorrecta())).toList());
    }

    public Pregunta toDomain(QuestionRecord r) {
        return Pregunta.reconstruir(r.getId(), new Enunciado(r.getEnunciado()), new Justificacion(r.getJustificacion()),
                r.getOpciones().stream().map(o -> new OpcionRespuesta(o.getTexto(), o.isCorrecta())).toList(),
                r.getEstado());
    }
}

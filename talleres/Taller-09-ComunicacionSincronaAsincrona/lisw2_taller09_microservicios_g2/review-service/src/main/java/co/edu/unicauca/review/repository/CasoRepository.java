package co.edu.unicauca.review.repository;

import co.edu.unicauca.review.model.CasoDeRevision;
import co.edu.unicauca.review.model.CasoEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CasoRepository extends JpaRepository<CasoDeRevision, Long> {

    boolean existsByEventId(String eventId);

    List<CasoDeRevision> findByQuestionIdOrderByRondaAsc(Long questionId);

    Optional<CasoDeRevision> findFirstByQuestionIdOrderByRondaDesc(Long questionId);

    Optional<CasoDeRevision> findFirstByQuestionIdAndEstadoOrderByRondaDesc(Long questionId, CasoEstado estado);

    /** Casos en curso donde el revisor todavía no ha evaluado. */
    @Query("select distinct c from CasoDeRevision c join c.asignaciones a "
            + "where a.reviewerId = :revisor and a.decision is null and c.estado = co.edu.unicauca.review.model.CasoEstado.EN_CURSO "
            + "order by c.id")
    List<CasoDeRevision> pendientesDe(@Param("revisor") Long revisor);
}

package co.edu.unicauca.history.repository;

import co.edu.unicauca.history.model.Entrada;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntradaRepository extends JpaRepository<Entrada, Long> {

    boolean existsByEventId(String eventId);

    List<Entrada> findByQuestionIdOrderByIdAsc(Long questionId);

    @Query("select e from Entrada e where :tipo is null or e.tipo = :tipo order by e.id desc")
    Page<Entrada> auditoria(@Param("tipo") String tipo, Pageable pageable);
}

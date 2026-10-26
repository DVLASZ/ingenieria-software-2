package com.example.questionbank.infrastructure.jpa.repository;

import com.example.questionbank.domain.model.QuestionStatus;
import com.example.questionbank.infrastructure.jpa.record.QuestionRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data interface for questions. Queries on the base class are polymorphic: they return both subtypes.
 */
public interface SpringQuestionDataRepository extends JpaRepository<QuestionRecord, Long> {

    /**
     * Finds the questions of an author.
     *
     * @param authorId identity of the author
     * @return the question records
     */
    List<QuestionRecord> findByAuthorIdOrderByIdAsc(Long authorId);

    /**
     * Finds the questions in a status.
     *
     * @param status the status
     * @return the question records
     */
    List<QuestionRecord> findByStatusOrderByIdAsc(QuestionStatus status);

    /**
     * Finds the questions that evaluate a competency and are in a status.
     *
     * @param competencyId identity of the competency
     * @param status the status
     * @return the question records
     */
    @Query("select distinct q from QuestionRecord q join q.competencies c "
            + "where c.id = :competencyId and q.status = :status order by q.id")
    List<QuestionRecord> findByCompetencyIdAndStatus(@Param("competencyId") Long competencyId,
                                                     @Param("status") QuestionStatus status);
}

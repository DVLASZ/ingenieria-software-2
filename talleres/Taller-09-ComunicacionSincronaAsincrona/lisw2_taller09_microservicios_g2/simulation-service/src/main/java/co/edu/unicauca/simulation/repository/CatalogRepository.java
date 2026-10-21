package co.edu.unicauca.simulation.repository;

import co.edu.unicauca.simulation.model.CatalogQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogRepository extends JpaRepository<CatalogQuestion, Long> {

    List<CatalogQuestion> findAllByOrderByQuestionIdAsc();
}

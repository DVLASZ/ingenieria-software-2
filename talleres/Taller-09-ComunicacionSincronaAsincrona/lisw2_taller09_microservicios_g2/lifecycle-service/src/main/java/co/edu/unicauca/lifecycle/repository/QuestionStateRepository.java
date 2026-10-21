package co.edu.unicauca.lifecycle.repository;

import co.edu.unicauca.lifecycle.model.QuestionState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionStateRepository extends JpaRepository<QuestionState, Long> {
}

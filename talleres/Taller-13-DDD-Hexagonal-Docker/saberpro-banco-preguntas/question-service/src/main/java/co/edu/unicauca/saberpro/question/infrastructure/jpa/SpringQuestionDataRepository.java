package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringQuestionDataRepository extends JpaRepository<QuestionRecord, String> {
}

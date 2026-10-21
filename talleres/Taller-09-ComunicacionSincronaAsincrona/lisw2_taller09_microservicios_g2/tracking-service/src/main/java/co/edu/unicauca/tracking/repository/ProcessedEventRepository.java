package co.edu.unicauca.tracking.repository;

import co.edu.unicauca.tracking.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}

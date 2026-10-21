package co.edu.unicauca.tracking.repository;

import co.edu.unicauca.tracking.model.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
}

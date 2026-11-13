package co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPreguntaRepository extends JpaRepository<PreguntaJpaEntity, String> {
}

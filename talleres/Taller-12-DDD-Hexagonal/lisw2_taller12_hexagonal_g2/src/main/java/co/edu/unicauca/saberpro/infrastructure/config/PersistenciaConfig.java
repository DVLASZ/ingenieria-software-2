package co.edu.unicauca.saberpro.infrastructure.config;

import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa.JpaQuestionAdapter;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa.SpringDataPreguntaRepository;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.memory.InMemoryQuestionAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Único sitio donde se elige el adaptador de salida. Se cambia con saberpro.persistencia=jpa|memoria. */
@Configuration
public class PersistenciaConfig {

    @Bean
    @ConditionalOnProperty(name = "saberpro.persistencia", havingValue = "jpa", matchIfMissing = true)
    PreguntaRepositoryPort jpaQuestionAdapter(SpringDataPreguntaRepository repositorio) {
        return new JpaQuestionAdapter(repositorio);
    }

    @Bean
    @ConditionalOnProperty(name = "saberpro.persistencia", havingValue = "memoria")
    PreguntaRepositoryPort inMemoryQuestionAdapter() {
        return new InMemoryQuestionAdapter();
    }
}

package co.edu.unicauca.saberpro.question.infrastructure.config;

import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.JpaQuestionAdapter;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.QuestionDomainMapper;
import co.edu.unicauca.saberpro.question.infrastructure.jpa.SpringQuestionDataRepository;
import co.edu.unicauca.saberpro.question.infrastructure.memory.InMemoryQuestionAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Único sitio donde se elige el adaptador de persistencia. Se cambia con saberpro.persistencia=jpa|memoria. */
@Configuration
public class PersistenciaConfig {

    @Bean
    @ConditionalOnProperty(name = "saberpro.persistencia", havingValue = "jpa", matchIfMissing = true)
    PreguntaRepositoryPort jpaQuestionAdapter(SpringQuestionDataRepository repositorio) {
        return new JpaQuestionAdapter(repositorio, new QuestionDomainMapper());
    }

    @Bean
    @ConditionalOnProperty(name = "saberpro.persistencia", havingValue = "memoria")
    PreguntaRepositoryPort inMemoryQuestionAdapter() {
        return new InMemoryQuestionAdapter();
    }
}

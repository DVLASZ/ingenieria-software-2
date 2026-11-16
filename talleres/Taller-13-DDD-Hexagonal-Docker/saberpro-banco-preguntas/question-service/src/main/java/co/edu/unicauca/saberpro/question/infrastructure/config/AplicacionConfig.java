package co.edu.unicauca.saberpro.question.infrastructure.config;

import co.edu.unicauca.saberpro.question.application.QuestionApplicationService;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionJustificacionPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AplicacionConfig {

    @Bean
    QuestionApplicationService questionApplicationService(PreguntaRepositoryPort repositorio,
                                                          NotificacionPort notificacion,
                                                          ValidacionJustificacionPort validacion) {
        return new QuestionApplicationService(repositorio, notificacion, validacion);
    }
}

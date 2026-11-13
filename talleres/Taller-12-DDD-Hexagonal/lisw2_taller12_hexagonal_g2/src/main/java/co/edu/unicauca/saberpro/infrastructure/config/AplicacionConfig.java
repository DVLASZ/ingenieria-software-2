package co.edu.unicauca.saberpro.infrastructure.config;

import co.edu.unicauca.saberpro.application.PreguntaService;
import co.edu.unicauca.saberpro.domain.ports.outbound.NotificadorPort;
import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.infrastructure.adapter.out.notification.ConsolaNotificadorAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AplicacionConfig {

    @Bean
    NotificadorPort notificador() {
        return new ConsolaNotificadorAdapter();
    }

    @Bean
    PreguntaService preguntaService(PreguntaRepositoryPort repositorio, NotificadorPort notificador) {
        return new PreguntaService(repositorio, notificador);
    }
}

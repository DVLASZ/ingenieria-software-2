package co.edu.unicauca.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Simula el envío del correo: lo escribe en el registro y espera un instante, como lo haría un servidor de correo lento.
 * Como corre en el hilo del listener, nunca bloquea la operación principal que originó el evento.
 */
@Component
public class EmailSimulator {

    private static final Logger log = LoggerFactory.getLogger(EmailSimulator.class);

    private final long retardoMs;

    public EmailSimulator(@Value("${app.notifications.delay-ms:250}") long retardoMs) {
        this.retardoMs = retardoMs;
    }

    public void enviar(Long destinatarioId, String asunto, String mensaje) {
        try {
            Thread.sleep(retardoMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("[correo simulado] para el usuario {}: {} | {}", destinatarioId, asunto, mensaje);
    }
}

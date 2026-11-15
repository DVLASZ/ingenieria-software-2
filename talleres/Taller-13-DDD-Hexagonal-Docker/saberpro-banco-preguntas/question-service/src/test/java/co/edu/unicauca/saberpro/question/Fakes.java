package co.edu.unicauca.saberpro.question;

import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionJustificacionPort;
import java.util.ArrayList;
import java.util.List;

/** Dobles de prueba para los puertos que hablan con otros servicios. */
public final class Fakes {

    private Fakes() {
    }

    public static class NotificacionFake implements NotificacionPort {

        public final List<String> solicitudes = new ArrayList<>();

        @Override
        public void solicitarRevision(Pregunta pregunta, String operationId) {
            solicitudes.add(pregunta.id() + ":" + operationId);
        }
    }

    public static class ValidacionFake implements ValidacionJustificacionPort {

        @Override
        public Resultado validar(String justificacion) {
            boolean ok = justificacion != null && justificacion.trim().length() >= 15;
            return new Resultado(ok, ok ? "Justificación válida" : "La justificación debe tener al menos 15 caracteres");
        }
    }
}

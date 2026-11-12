package co.edu.unicauca.saberpro.domain.ports.outbound;

import co.edu.unicauca.saberpro.domain.model.Pregunta;

/** Puerto de salida: avisar que una pregunta quedó lista para revisar. */
public interface NotificadorPort {

    void preguntaEnviadaARevision(Pregunta pregunta);
}

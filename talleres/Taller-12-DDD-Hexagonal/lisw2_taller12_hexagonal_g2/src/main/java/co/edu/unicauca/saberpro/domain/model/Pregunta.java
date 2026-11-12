package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Raíz del agregado. Nace en BORRADOR y solo pasa a PENDIENTE_REVISION si tiene exactamente 4 opciones
 * y una sola clave.
 */
public class Pregunta {

    public static final int OPCIONES_EXACTAS = 4;

    private final String id;
    private final Enunciado enunciado;
    private final Justificacion justificacion;
    private final List<OpcionRespuesta> opciones;
    private EstadoPregunta estado;

    private Pregunta(String id, Enunciado enunciado, Justificacion justificacion,
                     List<OpcionRespuesta> opciones, EstadoPregunta estado) {
        if (enunciado == null) {
            throw new ReglaNegocioException("La pregunta debe tener un enunciado.");
        }
        if (justificacion == null) {
            throw new ReglaNegocioException("La pregunta debe tener una justificación.");
        }
        List<OpcionRespuesta> copia = opciones == null ? new ArrayList<>() : new ArrayList<>(opciones);
        if (copia.size() > OPCIONES_EXACTAS) {
            throw new ReglaNegocioException(
                    "Una pregunta no puede tener más de " + OPCIONES_EXACTAS + " opciones y trae " + copia.size() + ".");
        }
        this.id = id;
        this.enunciado = enunciado;
        this.justificacion = justificacion;
        this.opciones = copia;
        this.estado = estado;
        if (estado == EstadoPregunta.PENDIENTE_REVISION) {
            verificarOpciones();
        }
    }

    public static Pregunta crear(Enunciado enunciado, Justificacion justificacion, List<OpcionRespuesta> opciones) {
        return new Pregunta(UUID.randomUUID().toString(), enunciado, justificacion, opciones, EstadoPregunta.BORRADOR);
    }

    /** La usan los adaptadores para armar de nuevo una pregunta que ya existía. Aplica las mismas reglas. */
    public static Pregunta reconstruir(String id, Enunciado enunciado, Justificacion justificacion,
                                       List<OpcionRespuesta> opciones, EstadoPregunta estado) {
        if (id == null || id.isBlank()) {
            throw new ReglaNegocioException("La pregunta reconstruida necesita un id.");
        }
        return new Pregunta(id, enunciado, justificacion, opciones, estado);
    }

    public void enviarARevision() {
        if (estado != EstadoPregunta.BORRADOR) {
            throw new ReglaNegocioException("Solo se envía a revisión una pregunta en BORRADOR y esta está en " + estado + ".");
        }
        verificarOpciones();
        estado = EstadoPregunta.PENDIENTE_REVISION;
    }

    private void verificarOpciones() {
        if (opciones.size() != OPCIONES_EXACTAS) {
            throw new ReglaNegocioException(
                    "La pregunta debe tener exactamente " + OPCIONES_EXACTAS + " opciones y tiene " + opciones.size() + ".");
        }
        long claves = opciones.stream().filter(OpcionRespuesta::esCorrecta).count();
        if (claves != 1) {
            throw new ReglaNegocioException("La pregunta debe tener una sola opción correcta y tiene " + claves + ".");
        }
    }

    public String id() {
        return id;
    }

    public Enunciado enunciado() {
        return enunciado;
    }

    public Justificacion justificacion() {
        return justificacion;
    }

    public List<OpcionRespuesta> opciones() {
        return Collections.unmodifiableList(opciones);
    }

    public EstadoPregunta estado() {
        return estado;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Pregunta otra && id.equals(otra.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

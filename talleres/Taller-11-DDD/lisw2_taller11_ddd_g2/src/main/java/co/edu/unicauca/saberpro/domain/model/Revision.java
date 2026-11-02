package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Raíz del agregado Revisión, del Contexto de Revisión por Pares. Reúne a los revisores asignados a una pregunta y
 * sus evaluaciones, y decide por unanimidad: un solo rechazo cierra la revisión como rechazada, y para aprobarla
 * todos los revisores tienen que aprobar. Se refiere a la pregunta solo por su identificador.
 */
public class Revision {

    public static final int MINIMO_REVISORES = 1;
    public static final int MAXIMO_REVISORES = 3;

    private Long id;
    private final Long preguntaId;
    private final List<Asignacion> asignaciones;
    private Dictamen dictamen;

    private Revision(Long preguntaId, List<Asignacion> asignaciones) {
        this.preguntaId = preguntaId;
        this.asignaciones = asignaciones;
    }

    public static Revision abrir(Long preguntaId, String autorId, List<Revisor> revisores) {
        if (preguntaId == null) {
            throw new ReglaNegocioException("La revisión necesita la pregunta que se revisa.");
        }
        if (revisores == null || revisores.size() < MINIMO_REVISORES || revisores.size() > MAXIMO_REVISORES) {
            throw new ReglaNegocioException(
                    "Una pregunta se asigna a entre " + MINIMO_REVISORES + " y " + MAXIMO_REVISORES + " revisores.");
        }
        if (new HashSet<>(revisores).size() != revisores.size()) {
            throw new ReglaNegocioException("Un revisor no se puede asignar dos veces a la misma pregunta.");
        }
        if (autorId != null && revisores.stream().anyMatch(r -> r.id().equals(autorId.trim()))) {
            throw new ReglaNegocioException("El autor no puede revisar su propia pregunta.");
        }
        List<Asignacion> asignaciones = new ArrayList<>();
        revisores.forEach(r -> asignaciones.add(Asignacion.de(r)));
        return new Revision(preguntaId, asignaciones);
    }

    public void evaluar(String revisorId, Dictamen veredicto, String observacion) {
        if (estaCerrada()) {
            throw new ReglaNegocioException("La revisión ya está cerrada con un dictamen " + dictamen + ".");
        }
        int posicion = posicionDe(revisorId);
        asignaciones.set(posicion, asignaciones.get(posicion).evaluar(new Evaluacion(revisorId, veredicto, observacion)));
        decidir();
    }

    private int posicionDe(String revisorId) {
        for (int i = 0; i < asignaciones.size(); i++) {
            if (asignaciones.get(i).revisor().id().equals(revisorId == null ? "" : revisorId.trim())) {
                return i;
            }
        }
        throw new ReglaNegocioException("El revisor no está asignado a esta pregunta.");
    }

    private void decidir() {
        List<Evaluacion> hechas = evaluaciones();
        if (hechas.stream().anyMatch(e -> e.dictamen() == Dictamen.RECHAZADO)) {
            dictamen = Dictamen.RECHAZADO;
        } else if (hechas.size() == asignaciones.size()) {
            dictamen = Dictamen.APROBADO;
        }
    }

    public boolean estaCerrada() {
        return dictamen != null;
    }

    public Optional<Dictamen> dictamen() {
        return Optional.ofNullable(dictamen);
    }

    public List<Evaluacion> evaluaciones() {
        return asignaciones.stream().filter(a -> !a.pendiente()).map(Asignacion::evaluacion).toList();
    }

    public List<Revisor> pendientes() {
        return asignaciones.stream().filter(Asignacion::pendiente).map(Asignacion::revisor).toList();
    }

    public List<Asignacion> asignaciones() {
        return Collections.unmodifiableList(asignaciones);
    }

    public Long preguntaId() {
        return preguntaId;
    }

    public Long id() {
        return id;
    }

    public void asignarIdentidad(Long nuevoId) {
        if (this.id != null) {
            throw new ReglaNegocioException("La revisión ya tiene identidad.");
        }
        this.id = Objects.requireNonNull(nuevoId, "La identidad no puede ser nula.");
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Revision otra && id != null && id.equals(otra.id));
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }
}

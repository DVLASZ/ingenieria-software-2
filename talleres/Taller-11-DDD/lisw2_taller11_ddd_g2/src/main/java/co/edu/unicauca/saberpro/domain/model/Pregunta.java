package co.edu.unicauca.saberpro.domain.model;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Raíz del agregado Pregunta. Las opciones, el enunciado y la justificación solo se tocan a través de ella, y ella
 * decide si un cambio es válido: nadie puede dejar una pregunta a medias en un estado que no le corresponde.
 */
public class Pregunta {

    private Long id;
    private final String autorId;
    private Enunciado enunciado;
    private Justificacion justificacion;
    private final List<OpcionRespuesta> opciones = new ArrayList<>();
    private EstadoPregunta estado = EstadoPregunta.BORRADOR;
    private int version = 1;

    public Pregunta() {
        this(null);
    }

    public Pregunta(String autorId) {
        this.autorId = autorId == null || autorId.isBlank() ? null : autorId.trim();
    }

    // ---- edición (solo en borrador)

    public void redactarEnunciado(String texto) {
        exigirBorrador("el enunciado");
        this.enunciado = new Enunciado(texto);
    }

    public void justificar(String texto) {
        exigirBorrador("la justificación");
        this.justificacion = new Justificacion(texto);
    }

    public void agregarOpcion(String texto, boolean esCorrecta) {
        exigirBorrador("las opciones");
        OpcionRespuesta nueva = new OpcionRespuesta(texto, esCorrecta);
        ValidezOpciones.verificarNuevaOpcion(opciones, nueva);
        opciones.add(nueva);
    }

    public void quitarOpcion(int posicion) {
        exigirBorrador("las opciones");
        if (posicion < 0 || posicion >= opciones.size()) {
            throw new ReglaNegocioException("No existe una opción en la posición " + posicion + ".");
        }
        opciones.remove(posicion);
    }

    // ---- ciclo de vida

    public void enviarARevision() {
        validarInvariantesParaEnvio();
        pasarA(EstadoPregunta.PENDIENTE_REVISION);
    }

    public void iniciarRevision() {
        pasarA(EstadoPregunta.EN_REVISION);
    }

    public void aprobar() {
        pasarA(EstadoPregunta.APROBADA);
    }

    public void rechazar() {
        pasarA(EstadoPregunta.RECHAZADA);
    }

    /** Una pregunta rechazada vuelve a borrador como una versión nueva, para corregirla y reenviarla. */
    public void reabrir() {
        pasarA(EstadoPregunta.BORRADOR);
        version++;
    }

    public void publicar() {
        pasarA(EstadoPregunta.PUBLICADA);
    }

    public void archivar() {
        pasarA(EstadoPregunta.ARCHIVADA);
    }

    // ---- invariantes

    private void validarInvariantesParaEnvio() {
        if (autorId == null) {
            throw new ReglaNegocioException("La pregunta debe tener un autor.");
        }
        if (enunciado == null) {
            throw new ReglaNegocioException("La pregunta debe tener un enunciado.");
        }
        if (justificacion == null) {
            throw new ReglaNegocioException("La pregunta debe tener una justificación.");
        }
        ValidezOpciones.verificarParaEnvio(opciones);
    }

    private void exigirBorrador(String que) {
        if (estado != EstadoPregunta.BORRADOR) {
            throw new ReglaNegocioException("Solo se puede modificar " + que + " en estado Borrador.");
        }
    }

    private void pasarA(EstadoPregunta destino) {
        if (!estado.puedePasarA(destino)) {
            throw new ReglaNegocioException("No se puede pasar de " + estado + " a " + destino + ".");
        }
        estado = destino;
    }

    // ---- consulta

    public Long id() {
        return id;
    }

    /** La identidad la asigna quien guarda la pregunta, una sola vez. */
    public void asignarIdentidad(Long nuevoId) {
        if (this.id != null) {
            throw new ReglaNegocioException("La pregunta ya tiene identidad.");
        }
        this.id = Objects.requireNonNull(nuevoId, "La identidad no puede ser nula.");
    }

    public String autorId() {
        return autorId;
    }

    public Optional<Enunciado> enunciado() {
        return Optional.ofNullable(enunciado);
    }

    public Optional<Justificacion> justificacion() {
        return Optional.ofNullable(justificacion);
    }

    public List<OpcionRespuesta> opciones() {
        return Collections.unmodifiableList(opciones);
    }

    public Optional<OpcionRespuesta> clave() {
        return opciones.stream().filter(OpcionRespuesta::esCorrecta).findFirst();
    }

    public EstadoPregunta estado() {
        return estado;
    }

    public int version() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Pregunta otra && id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }
}

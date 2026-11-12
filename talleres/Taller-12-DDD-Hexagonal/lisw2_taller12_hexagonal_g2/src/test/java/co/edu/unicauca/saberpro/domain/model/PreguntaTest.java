package co.edu.unicauca.saberpro.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PreguntaTest {

    static final Enunciado ENUNCIADO = new Enunciado("¿Qué separa el dominio de la infraestructura?");
    static final Justificacion JUSTIFICACION = new Justificacion("El puerto es el contrato que el dominio define");

    static List<OpcionRespuesta> cuatroOpciones() {
        return List.of(new OpcionRespuesta("Un puerto", true), new OpcionRespuesta("Una tabla", false),
                new OpcionRespuesta("Un controlador", false), new OpcionRespuesta("Un script", false));
    }

    @Test
    void naceEnBorradorConIdentidad() {
        Pregunta p = Pregunta.crear(ENUNCIADO, JUSTIFICACION, cuatroOpciones());
        assertEquals(EstadoPregunta.BORRADOR, p.estado());
        assertEquals(36, p.id().length());
    }

    @Test
    void dosPreguntasNuevasTienenIdentidadDistinta() {
        assertNotEquals(Pregunta.crear(ENUNCIADO, JUSTIFICACION, cuatroOpciones()),
                Pregunta.crear(ENUNCIADO, JUSTIFICACION, cuatroOpciones()));
    }

    @Test
    void conCuatroOpcionesYUnaClavePasaARevision() {
        Pregunta p = Pregunta.crear(ENUNCIADO, JUSTIFICACION, cuatroOpciones());
        p.enviarARevision();
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, p.estado());
    }

    @Test
    void conTresOpcionesNoPasaARevision() {
        Pregunta p = Pregunta.crear(ENUNCIADO, JUSTIFICACION, cuatroOpciones().subList(0, 3));
        assertThrows(ReglaNegocioException.class, p::enviarARevision);
        assertEquals(EstadoPregunta.BORRADOR, p.estado());
    }

    @Test
    void sinOpcionesNoPasaARevision() {
        Pregunta p = Pregunta.crear(ENUNCIADO, JUSTIFICACION, null);
        assertThrows(ReglaNegocioException.class, p::enviarARevision);
    }

    @Test
    void sinClaveNoPasaARevision() {
        List<OpcionRespuesta> sinClave = List.of(new OpcionRespuesta("A", false), new OpcionRespuesta("B", false),
                new OpcionRespuesta("C", false), new OpcionRespuesta("D", false));
        Pregunta p = Pregunta.crear(ENUNCIADO, JUSTIFICACION, sinClave);
        assertThrows(ReglaNegocioException.class, p::enviarARevision);
    }

    @Test
    void conDosClavesNoPasaARevision() {
        List<OpcionRespuesta> dosClaves = List.of(new OpcionRespuesta("A", true), new OpcionRespuesta("B", true),
                new OpcionRespuesta("C", false), new OpcionRespuesta("D", false));
        Pregunta p = Pregunta.crear(ENUNCIADO, JUSTIFICACION, dosClaves);
        assertThrows(ReglaNegocioException.class, p::enviarARevision);
    }

    @Test
    void conMasDeCuatroOpcionesNiSiquieraSeCrea() {
        List<OpcionRespuesta> cinco = new ArrayList<>(cuatroOpciones());
        cinco.add(new OpcionRespuesta("E", false));
        assertThrows(ReglaNegocioException.class, () -> Pregunta.crear(ENUNCIADO, JUSTIFICACION, cinco));
    }

    @Test
    void unaPreguntaPendienteNoSeEnviaDeNuevo() {
        Pregunta p = Pregunta.crear(ENUNCIADO, JUSTIFICACION, cuatroOpciones());
        p.enviarARevision();
        assertThrows(ReglaNegocioException.class, p::enviarARevision);
    }

    @Test
    void exigeEnunciadoYJustificacion() {
        assertThrows(ReglaNegocioException.class, () -> Pregunta.crear(null, JUSTIFICACION, cuatroOpciones()));
        assertThrows(ReglaNegocioException.class, () -> Pregunta.crear(ENUNCIADO, null, cuatroOpciones()));
    }

    @Test
    void laListaDeOpcionesNoSePuedeModificarDesdeAfuera() {
        Pregunta p = Pregunta.crear(ENUNCIADO, JUSTIFICACION, cuatroOpciones());
        assertThrows(UnsupportedOperationException.class, () -> p.opciones().add(new OpcionRespuesta("E", false)));
    }

    @Test
    void cambiarLaListaOriginalNoAfectaALaPregunta() {
        List<OpcionRespuesta> original = new ArrayList<>(cuatroOpciones());
        Pregunta p = Pregunta.crear(ENUNCIADO, JUSTIFICACION, original);
        original.clear();
        assertEquals(4, p.opciones().size());
    }

    @Test
    void reconstruirPendienteConOpcionesInvalidasSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> Pregunta.reconstruir("abc", ENUNCIADO, JUSTIFICACION,
                cuatroOpciones().subList(0, 2), EstadoPregunta.PENDIENTE_REVISION));
    }

    @Test
    void reconstruirConservaIdYEstado() {
        Pregunta p = Pregunta.reconstruir("abc", ENUNCIADO, JUSTIFICACION, cuatroOpciones(),
                EstadoPregunta.PENDIENTE_REVISION);
        assertEquals("abc", p.id());
        assertEquals(EstadoPregunta.PENDIENTE_REVISION, p.estado());
    }

    @Test
    void reconstruirSinIdSeRechaza() {
        assertThrows(ReglaNegocioException.class,
                () -> Pregunta.reconstruir(" ", ENUNCIADO, JUSTIFICACION, cuatroOpciones(), EstadoPregunta.BORRADOR));
    }
}

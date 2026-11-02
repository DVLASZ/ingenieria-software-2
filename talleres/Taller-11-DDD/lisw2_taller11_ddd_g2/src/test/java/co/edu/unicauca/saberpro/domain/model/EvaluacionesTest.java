package co.edu.unicauca.saberpro.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

class EvaluacionesTest {

    private static final Revisor ANA = new Revisor("d-1", "Ana");

    @Test
    void elRevisorNecesitaIdentificadorYNombre() {
        assertThrows(ReglaNegocioException.class, () -> new Revisor(null, "Ana"));
        assertThrows(ReglaNegocioException.class, () -> new Revisor(" ", "Ana"));
        assertThrows(ReglaNegocioException.class, () -> new Revisor("d-1", null));
    }

    @Test
    void dosRevisoresConElMismoIdSonLaMismaPersona() {
        assertEquals(new Revisor("d-1", "Ana"), new Revisor("d-1", "Ana María"));
        assertFalse(new Revisor("d-1", "Ana").equals(new Revisor("d-2", "Ana")));
    }

    @Test
    void rechazarExigeUnaObservacion() {
        assertThrows(ReglaNegocioException.class, () -> new Evaluacion("d-1", Dictamen.RECHAZADO, " "));
        assertThrows(ReglaNegocioException.class, () -> new Evaluacion("d-1", Dictamen.RECHAZADO, null));
        assertEquals("", new Evaluacion("d-1", Dictamen.APROBADO, null).observacion());
    }

    @Test
    void laEvaluacionNecesitaRevisorYDictamen() {
        assertThrows(ReglaNegocioException.class, () -> new Evaluacion(null, Dictamen.APROBADO, ""));
        assertThrows(ReglaNegocioException.class, () -> new Evaluacion("d-1", null, ""));
    }

    @Test
    void unaAsignacionEvaluadaDevuelveOtraYNoSeEvaluaDosVeces() {
        Asignacion pendiente = Asignacion.de(ANA);
        Asignacion hecha = pendiente.evaluar(new Evaluacion("d-1", Dictamen.APROBADO, ""));

        assertTrue(pendiente.pendiente());
        assertFalse(hecha.pendiente());
        assertThrows(ReglaNegocioException.class, () -> hecha.evaluar(new Evaluacion("d-1", Dictamen.APROBADO, "")));
    }

    @Test
    void laAsignacionNoAceptaLaEvaluacionDeOtroRevisor() {
        assertThrows(ReglaNegocioException.class,
                () -> new Asignacion(ANA, new Evaluacion("d-2", Dictamen.APROBADO, "")));
    }
}

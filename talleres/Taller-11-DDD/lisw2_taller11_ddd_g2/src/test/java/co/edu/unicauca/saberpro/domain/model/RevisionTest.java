package co.edu.unicauca.saberpro.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.saberpro.domain.exception.ReglaNegocioException;
import java.util.List;
import org.junit.jupiter.api.Test;

class RevisionTest {

    private static final Revisor ANA = new Revisor("d-1", "Ana");
    private static final Revisor BEA = new Revisor("d-2", "Beatriz");
    private static final Revisor CARLOS = new Revisor("d-3", "Carlos");
    private static final Revisor DIEGO = new Revisor("d-4", "Diego");

    private static Revision con(Revisor... revisores) {
        return Revision.abrir(10L, "autor-1", List.of(revisores));
    }

    // asignación de revisores

    @Test
    void sePuedeAsignarDeUnoATresRevisores() {
        assertEquals(1, con(ANA).asignaciones().size());
        assertEquals(3, con(ANA, BEA, CARLOS).asignaciones().size());
    }

    @Test
    void noSeAsignanCeroNiMasDeTresRevisores() {
        assertThrows(ReglaNegocioException.class, () -> Revision.abrir(10L, "autor-1", List.of()));
        assertThrows(ReglaNegocioException.class, () -> con(ANA, BEA, CARLOS, DIEGO));
        assertThrows(ReglaNegocioException.class, () -> Revision.abrir(10L, "autor-1", null));
    }

    @Test
    void elAutorNoPuedeRevisarSuPropiaPregunta() {
        assertThrows(ReglaNegocioException.class, () -> Revision.abrir(10L, "d-1", List.of(ANA, BEA)));
    }

    @Test
    void unRevisorNoSeRepite() {
        assertThrows(ReglaNegocioException.class, () -> con(ANA, ANA));
    }

    @Test
    void laRevisionNecesitaLaPregunta() {
        assertThrows(ReglaNegocioException.class, () -> Revision.abrir(null, "autor-1", List.of(ANA)));
    }

    // decisión por unanimidad

    @Test
    void sinEvaluacionesNoHayDictamen() {
        Revision r = con(ANA, BEA);

        assertTrue(r.dictamen().isEmpty());
        assertFalse(r.estaCerrada());
        assertEquals(2, r.pendientes().size());
    }

    @Test
    void unaAprobacionNoBastaSiFaltanRevisores() {
        Revision r = con(ANA, BEA);

        r.evaluar("d-1", Dictamen.APROBADO, "");

        assertTrue(r.dictamen().isEmpty());
        assertEquals(List.of(BEA), r.pendientes());
    }

    @Test
    void conTodasLasAprobacionesSeApruebaPorUnanimidad() {
        Revision r = con(ANA, BEA, CARLOS);

        r.evaluar("d-1", Dictamen.APROBADO, "");
        r.evaluar("d-2", Dictamen.APROBADO, "Clara");
        r.evaluar("d-3", Dictamen.APROBADO, "");

        assertEquals(Dictamen.APROBADO, r.dictamen().orElseThrow());
        assertEquals(3, r.evaluaciones().size());
        assertTrue(r.pendientes().isEmpty());
    }

    @Test
    void unSoloRechazoCierraLaRevisionSinEsperarALosDemas() {
        Revision r = con(ANA, BEA, CARLOS);

        r.evaluar("d-2", Dictamen.RECHAZADO, "La clave es discutible");

        assertEquals(Dictamen.RECHAZADO, r.dictamen().orElseThrow());
        assertTrue(r.estaCerrada());
        assertEquals(2, r.pendientes().size());
    }

    @Test
    void despuesDeCerrarNadieMasEvalua() {
        Revision r = con(ANA, BEA);
        r.evaluar("d-1", Dictamen.RECHAZADO, "No cumple");

        assertThrows(ReglaNegocioException.class, () -> r.evaluar("d-2", Dictamen.APROBADO, ""));
    }

    @Test
    void soloEvaluanLosRevisoresAsignados() {
        Revision r = con(ANA, BEA);

        assertThrows(ReglaNegocioException.class, () -> r.evaluar("d-9", Dictamen.APROBADO, ""));
        assertThrows(ReglaNegocioException.class, () -> r.evaluar(null, Dictamen.APROBADO, ""));
    }

    @Test
    void unRevisorNoEvaluaDosVeces() {
        Revision r = con(ANA, BEA);
        r.evaluar("d-1", Dictamen.APROBADO, "");

        assertThrows(ReglaNegocioException.class, () -> r.evaluar("d-1", Dictamen.APROBADO, ""));
    }

    @Test
    void unaEvaluacionInvalidaNoCambiaLaRevision() {
        Revision r = con(ANA, BEA);

        assertThrows(ReglaNegocioException.class, () -> r.evaluar("d-1", Dictamen.RECHAZADO, ""));

        assertTrue(r.dictamen().isEmpty());
        assertEquals(2, r.pendientes().size());
    }

    @Test
    void laIdentidadDeLaRevisionSeAsignaUnaVez() {
        Revision r = con(ANA);

        r.asignarIdentidad(3L);

        assertEquals(3L, r.id());
        assertThrows(ReglaNegocioException.class, () -> r.asignarIdentidad(4L));
    }

    @Test
    void laRevisionGuardaSoloElIdentificadorDeLaPregunta() {
        assertEquals(10L, con(ANA).preguntaId());
    }
}

package co.edu.unicauca.saberpro.domain.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

class ExcepcionesTest {

    @Test
    void laReglaDeNegocioEsUnaExcepcionDeDominio() {
        ReglaNegocioException e = new ReglaNegocioException("no se puede");

        assertInstanceOf(DomainException.class, e);
        assertInstanceOf(RuntimeException.class, e);
        assertEquals("no se puede", e.getMessage());
    }
}

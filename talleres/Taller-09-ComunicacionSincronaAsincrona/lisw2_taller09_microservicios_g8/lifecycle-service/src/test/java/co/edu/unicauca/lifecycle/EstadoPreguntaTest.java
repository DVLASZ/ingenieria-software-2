package co.edu.unicauca.lifecycle;

import co.edu.unicauca.lifecycle.model.EstadoPregunta;
import org.junit.jupiter.api.Test;

import static co.edu.unicauca.lifecycle.model.EstadoPregunta.APROBADA;
import static co.edu.unicauca.lifecycle.model.EstadoPregunta.ARCHIVADA;
import static co.edu.unicauca.lifecycle.model.EstadoPregunta.BORRADOR;
import static co.edu.unicauca.lifecycle.model.EstadoPregunta.EN_REVISION;
import static co.edu.unicauca.lifecycle.model.EstadoPregunta.PENDIENTE_REVISION;
import static co.edu.unicauca.lifecycle.model.EstadoPregunta.PUBLICADA;
import static co.edu.unicauca.lifecycle.model.EstadoPregunta.RECHAZADA;
import static org.assertj.core.api.Assertions.assertThat;

class EstadoPreguntaTest {

    @Test
    void elFlujoNormalEsValido() {
        assertThat(BORRADOR.puedePasarA(PENDIENTE_REVISION)).isTrue();
        assertThat(PENDIENTE_REVISION.puedePasarA(EN_REVISION)).isTrue();
        assertThat(EN_REVISION.puedePasarA(APROBADA)).isTrue();
        assertThat(EN_REVISION.puedePasarA(RECHAZADA)).isTrue();
        assertThat(APROBADA.puedePasarA(PUBLICADA)).isTrue();
    }

    @Test
    void unaPreguntaRechazadaSoloVuelveABorrador() {
        assertThat(RECHAZADA.puedePasarA(BORRADOR)).isTrue();
        assertThat(RECHAZADA.puedePasarA(PENDIENTE_REVISION)).isFalse();
        assertThat(RECHAZADA.puedePasarA(APROBADA)).isFalse();
    }

    @Test
    void noSePuedeSaltarEtapas() {
        assertThat(BORRADOR.puedePasarA(APROBADA)).isFalse();
        assertThat(BORRADOR.puedePasarA(EN_REVISION)).isFalse();
        assertThat(PENDIENTE_REVISION.puedePasarA(APROBADA)).isFalse();
        assertThat(APROBADA.puedePasarA(BORRADOR)).isFalse();
    }

    @Test
    void cualquierEstadoPuedeArchivarseYDeArchivadaNoSeSale() {
        for (EstadoPregunta e : EstadoPregunta.values()) {
            assertThat(e.puedePasarA(ARCHIVADA)).isTrue();
        }
        assertThat(ARCHIVADA.puedePasarA(BORRADOR)).isFalse();
        assertThat(PUBLICADA.puedePasarA(BORRADOR)).isFalse();
    }

    @Test
    void cadaEstadoTieneSuEquivalenteEnIngles() {
        assertThat(BORRADOR.equivalente()).isEqualTo("DRAFT");
        assertThat(PENDIENTE_REVISION.equivalente()).isEqualTo("PENDING_REVIEW");
        assertThat(EN_REVISION.equivalente()).isEqualTo("IN_REVIEW");
        assertThat(APROBADA.equivalente()).isEqualTo("APPROVED");
        assertThat(RECHAZADA.equivalente()).isEqualTo("REJECTED");
        assertThat(PUBLICADA.equivalente()).isEqualTo("PUBLISHED");
        assertThat(ARCHIVADA.equivalente()).isEqualTo("ARCHIVED");
    }
}

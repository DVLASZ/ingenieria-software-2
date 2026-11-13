package co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class OpcionEmbeddable {

    @Column(nullable = false, length = 1000)
    private String texto;

    @Column(nullable = false)
    private boolean correcta;

    protected OpcionEmbeddable() {
    }

    public OpcionEmbeddable(String texto, boolean correcta) {
        this.texto = texto;
        this.correcta = correcta;
    }

    public String getTexto() {
        return texto;
    }

    public boolean isCorrecta() {
        return correcta;
    }
}

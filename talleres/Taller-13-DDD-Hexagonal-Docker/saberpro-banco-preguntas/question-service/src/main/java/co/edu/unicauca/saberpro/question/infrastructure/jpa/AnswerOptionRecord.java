package co.edu.unicauca.saberpro.question.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AnswerOptionRecord {

    @Column(nullable = false, length = 1000)
    private String texto;

    @Column(nullable = false)
    private boolean correcta;

    protected AnswerOptionRecord() {
    }

    public AnswerOptionRecord(String texto, boolean correcta) {
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

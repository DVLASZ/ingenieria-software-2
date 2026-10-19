package co.edu.unicauca.simulation.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/** Pregunta publicada, copiada al catálogo propio de simulacros (se alimenta del evento `question.published`). */
@Entity
@Table(name = "catalogo")
public class CatalogQuestion {

    @Id
    private Long questionId;

    private String titulo;
    private String competencia;

    @Column(length = 4000)
    private String contexto;

    @Column(length = 2000)
    private String pregunta;

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    @Column(length = 600)
    private List<String> opciones = new ArrayList<>();

    private String respuestaCorrecta;

    protected CatalogQuestion() {
    }

    public CatalogQuestion(Long questionId, String titulo, String competencia, String contexto, String pregunta,
                           List<String> opciones, String respuestaCorrecta) {
        this.questionId = questionId;
        this.titulo = titulo;
        this.competencia = competencia;
        this.contexto = contexto;
        this.pregunta = pregunta;
        this.opciones = new ArrayList<>(opciones);
        this.respuestaCorrecta = respuestaCorrecta;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCompetencia() {
        return competencia;
    }

    public String getContexto() {
        return contexto;
    }

    public String getPregunta() {
        return pregunta;
    }

    public List<String> getOpciones() {
        return opciones;
    }

    public String getRespuestaCorrecta() {
        return respuestaCorrecta;
    }
}

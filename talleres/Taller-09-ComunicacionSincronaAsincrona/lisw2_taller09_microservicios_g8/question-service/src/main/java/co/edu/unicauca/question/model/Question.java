package co.edu.unicauca.question.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Contenido de una pregunta de selección múltiple con única respuesta. */
@Entity
@Table(name = "preguntas")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long autorId;
    private String titulo;

    @Column(length = 4000)
    private String contexto;

    @Column(length = 2000)
    private String pregunta;

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    @Column(length = 600)
    private List<String> opciones = new ArrayList<>();

    private String respuestaCorrecta;

    @Column(length = 2000)
    private String justificacion;

    @Column(length = 1000)
    private String bibliografia;

    private String competencia;
    private String tema;
    private String subtema;
    private String dificultad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPregunta estado = EstadoPregunta.BORRADOR;

    private int version = 1;
    private Instant creadaEn = Instant.now();
    private Instant actualizadaEn = Instant.now();

    public void actualizarContenido(String titulo, String contexto, String pregunta, List<String> opciones, String respuestaCorrecta,
                                    String justificacion, String bibliografia, String competencia, String tema, String subtema,
                                    String dificultad) {
        this.titulo = titulo;
        this.contexto = contexto;
        this.pregunta = pregunta;
        this.opciones = new ArrayList<>(opciones);
        this.respuestaCorrecta = respuestaCorrecta;
        this.justificacion = justificacion;
        this.bibliografia = bibliografia;
        this.competencia = competencia;
        this.tema = tema;
        this.subtema = subtema;
        this.dificultad = dificultad;
        this.actualizadaEn = Instant.now();
    }

    public void nuevaVersion() {
        this.version++;
    }

    public void setEstado(EstadoPregunta estado) {
        this.estado = estado;
        this.actualizadaEn = Instant.now();
    }

    public void setAutorId(Long autorId) {
        this.autorId = autorId;
    }

    public Long getId() {
        return id;
    }

    public Long getAutorId() {
        return autorId;
    }

    public String getTitulo() {
        return titulo;
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

    public String getJustificacion() {
        return justificacion;
    }

    public String getBibliografia() {
        return bibliografia;
    }

    public String getCompetencia() {
        return competencia;
    }

    public String getTema() {
        return tema;
    }

    public String getSubtema() {
        return subtema;
    }

    public String getDificultad() {
        return dificultad;
    }

    public EstadoPregunta getEstado() {
        return estado;
    }

    public int getVersion() {
        return version;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }

    public Instant getActualizadaEn() {
        return actualizadaEn;
    }
}

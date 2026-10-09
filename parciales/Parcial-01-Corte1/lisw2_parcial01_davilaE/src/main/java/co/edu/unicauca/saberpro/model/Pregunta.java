package co.edu.unicauca.saberpro.model;

import java.util.List;
import java.util.ArrayList;

public class Pregunta {
    private int id;
    private String enunciado;
    private List<String> opciones;
    private String respuestaCorrecta;
    private String estado;
    private int puntaje;
    private String competencia;

    public Pregunta(int id, String enunciado, List<String> opciones, String respuestaCorrecta, String estado, int puntaje, String competencia) {
        this.id = id;
        this.enunciado = enunciado;
        this.opciones = (opciones != null) ? opciones : new ArrayList<>();
        this.respuestaCorrecta = respuestaCorrecta;
        this.estado = estado;
        this.puntaje = puntaje;
        this.competencia = competencia;
    }

    public int getId() { return id; }
    public String getEnunciado() { return enunciado; }
    public List<String> getOpciones() { return opciones; }
    public String getRespuestaCorrecta() { return respuestaCorrecta; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getPuntaje() { return puntaje; }
    public void setPuntaje(int puntaje) { this.puntaje = puntaje; }
    public String getCompetencia() { return competencia; }
}

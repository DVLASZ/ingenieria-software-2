package co.edu.unicauca.tracking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Acumulado de un usuario a partir de los simulacros que ha resuelto. */
@Entity
@Table(name = "estadisticas_usuario")
public class UserStats {

    @Id
    private Long userId;

    private int simulacros;
    private int correctas;
    private int preguntas;
    private double sumaPuntajes;

    protected UserStats() {
    }

    public UserStats(Long userId) {
        this.userId = userId;
    }

    public void sumar(int correctas, int preguntas, double puntaje) {
        this.simulacros++;
        this.correctas += correctas;
        this.preguntas += preguntas;
        this.sumaPuntajes += puntaje;
    }

    public double promedio() {
        return simulacros == 0 ? 0 : Math.round(10.0 * sumaPuntajes / simulacros) / 10.0;
    }

    public Long getUserId() {
        return userId;
    }

    public int getSimulacros() {
        return simulacros;
    }

    public int getCorrectas() {
        return correctas;
    }

    public int getPreguntas() {
        return preguntas;
    }

    public double getSumaPuntajes() {
        return sumaPuntajes;
    }
}

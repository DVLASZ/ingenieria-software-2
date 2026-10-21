package co.edu.unicauca.simulation.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "simulacros")
public class Simulacro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SimulacroEstado estado = SimulacroEstado.CREADO;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Long> preguntaIds = new ArrayList<>();

    private Integer correctas;
    private Integer total;
    private Double puntaje;
    private Instant creadoEn = Instant.now();
    private Instant resueltoEn;

    protected Simulacro() {
    }

    public Simulacro(Long userId, List<Long> preguntaIds) {
        this.userId = userId;
        this.preguntaIds = new ArrayList<>(preguntaIds);
    }

    public void resolver(int correctas, int total, double puntaje) {
        this.estado = SimulacroEstado.RESUELTO;
        this.correctas = correctas;
        this.total = total;
        this.puntaje = puntaje;
        this.resueltoEn = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public SimulacroEstado getEstado() {
        return estado;
    }

    public List<Long> getPreguntaIds() {
        return preguntaIds;
    }

    public Integer getCorrectas() {
        return correctas;
    }

    public Integer getTotal() {
        return total;
    }

    public Double getPuntaje() {
        return puntaje;
    }
}

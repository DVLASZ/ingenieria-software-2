package co.edu.unicauca.review.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Una ronda de revisión de una pregunta: se abre al enviarla y se cierra por unanimidad o con un rechazo. */
@Entity
@Table(name = "casos_de_revision")
public class CasoDeRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long questionId;
    private int ronda;
    private Long autorId;
    private String titulo;

    @Column(unique = true)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CasoEstado estado = CasoEstado.PENDIENTE_ASIGNACION;

    /** `APPROVED` o `REJECTED` cuando la ronda se cierra. */
    private String resultado;

    private Long asignadoPor;
    private Instant creadoEn = Instant.now();
    private Instant cerradoEn;

    @OneToMany(mappedBy = "caso", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id")
    private List<Asignacion> asignaciones = new ArrayList<>();

    protected CasoDeRevision() {
    }

    public CasoDeRevision(Long questionId, int ronda, Long autorId, String titulo, String eventId) {
        this.questionId = questionId;
        this.ronda = ronda;
        this.autorId = autorId;
        this.titulo = titulo;
        this.eventId = eventId;
    }

    public void asignar(List<Long> revisores, Long administrador) {
        revisores.forEach(r -> asignaciones.add(new Asignacion(this, r)));
        this.asignadoPor = administrador;
        this.estado = CasoEstado.EN_CURSO;
    }

    public void cerrar(String resultado) {
        this.estado = CasoEstado.CERRADA;
        this.resultado = resultado;
        this.cerradoEn = Instant.now();
    }

    public boolean todasEvaluadas() {
        return !asignaciones.isEmpty() && asignaciones.stream().allMatch(a -> a.getDecision() != null);
    }

    public Long getId() {
        return id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public int getRonda() {
        return ronda;
    }

    public Long getAutorId() {
        return autorId;
    }

    public String getTitulo() {
        return titulo;
    }

    public CasoEstado getEstado() {
        return estado;
    }

    public String getResultado() {
        return resultado;
    }

    public Long getAsignadoPor() {
        return asignadoPor;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getCerradoEn() {
        return cerradoEn;
    }

    public List<Asignacion> getAsignaciones() {
        return asignaciones;
    }
}

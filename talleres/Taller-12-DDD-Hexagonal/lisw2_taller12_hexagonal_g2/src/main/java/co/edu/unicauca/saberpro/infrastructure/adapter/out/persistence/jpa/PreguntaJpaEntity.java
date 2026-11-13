package co.edu.unicauca.saberpro.infrastructure.adapter.out.persistence.jpa;

import co.edu.unicauca.saberpro.domain.model.EstadoPregunta;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "preguntas")
public class PreguntaJpaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 2000)
    private String enunciado;

    @Column(nullable = false, length = 2000)
    private String justificacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPregunta estado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "opciones_pregunta", joinColumns = @JoinColumn(name = "pregunta_id"))
    @OrderColumn(name = "posicion")
    private List<OpcionEmbeddable> opciones = new ArrayList<>();

    protected PreguntaJpaEntity() {
    }

    public PreguntaJpaEntity(String id, String enunciado, String justificacion, EstadoPregunta estado,
                             List<OpcionEmbeddable> opciones) {
        this.id = id;
        this.enunciado = enunciado;
        this.justificacion = justificacion;
        this.estado = estado;
        this.opciones = new ArrayList<>(opciones);
    }

    public String getId() {
        return id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public EstadoPregunta getEstado() {
        return estado;
    }

    public List<OpcionEmbeddable> getOpciones() {
        return opciones;
    }
}

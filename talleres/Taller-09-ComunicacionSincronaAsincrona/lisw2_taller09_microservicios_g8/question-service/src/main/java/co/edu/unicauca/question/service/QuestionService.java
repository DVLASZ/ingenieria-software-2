package co.edu.unicauca.question.service;

import co.edu.unicauca.question.client.LifecycleClient;
import co.edu.unicauca.question.client.UserClient;
import co.edu.unicauca.question.client.ValidationClient;
import co.edu.unicauca.question.controller.ApiException;
import co.edu.unicauca.question.dto.Dtos.PreguntaRequest;
import co.edu.unicauca.question.dto.Dtos.ResultadoValidacion;
import co.edu.unicauca.question.dto.Dtos.UsuarioInfo;
import co.edu.unicauca.question.messaging.CommandPublisher;
import co.edu.unicauca.question.model.Comando;
import co.edu.unicauca.question.model.EstadoPregunta;
import co.edu.unicauca.question.model.Operation;
import co.edu.unicauca.question.model.OperationStatus;
import co.edu.unicauca.question.model.Question;
import co.edu.unicauca.question.repository.OperationRepository;
import co.edu.unicauca.question.repository.QuestionRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);
    private static final Set<EstadoPregunta> EDITABLES = EnumSet.of(EstadoPregunta.BORRADOR, EstadoPregunta.RECHAZADA);

    private final QuestionRepository preguntas;
    private final OperationRepository operaciones;
    private final ValidationClient validacion;
    private final LifecycleClient ciclo;
    private final UserClient usuarios;
    private final CommandPublisher comandos;

    public QuestionService(QuestionRepository preguntas, OperationRepository operaciones, ValidationClient validacion,
                           LifecycleClient ciclo, UserClient usuarios, CommandPublisher comandos) {
        this.preguntas = preguntas;
        this.operaciones = operaciones;
        this.validacion = validacion;
        this.ciclo = ciclo;
        this.usuarios = usuarios;
        this.comandos = comandos;
    }

    /** Creación síncrona: comprueba el autor en user-service, valida por REST, guarda en Borrador y crea el estado en lifecycle. */
    public Question crear(PreguntaRequest req) {
        exigirAutor(req.autorId());
        validar(req);
        Question q = new Question();
        q.setAutorId(req.autorId());
        aplicar(q, req);
        q = preguntas.save(q);
        try {
            ciclo.crearEstado(q.getId(), q.getAutorId(), q.getTitulo(), q.getCompetencia());
        } catch (RuntimeException e) {
            preguntas.delete(q);
            throw e;
        }
        return q;
    }

    /** Solo se edita en Borrador o Rechazada; una rechazada vuelve primero a Borrador (corrección del autor). */
    public Question actualizar(Long id, PreguntaRequest req) {
        Question q = obtener(id);
        if (!q.getAutorId().equals(req.autorId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo el autor puede editar su pregunta");
        }
        if (!EDITABLES.contains(q.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se puede editar una pregunta en Borrador o Rechazada; está " + q.getEstado().etiqueta());
        }
        exigirAutor(req.autorId());
        validar(req);
        if (q.getEstado() == EstadoPregunta.RECHAZADA) {
            ciclo.transicionar(q.getId(), EstadoPregunta.BORRADOR.name(), String.valueOf(req.autorId()), "Corrección del autor");
            q.setEstado(EstadoPregunta.BORRADOR);
        }
        aplicar(q, req);
        q.nuevaVersion();
        return preguntas.save(q);
    }

    public Question obtener(Long id) {
        return preguntas.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe la pregunta " + id));
    }

    public Page<Question> listar(EstadoPregunta estado, String competencia, String tema, Long autorId, int pagina, int tamano) {
        Specification<Question> filtro = (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (estado != null) {
                condiciones.add(cb.equal(raiz.get("estado"), estado));
            }
            if (competencia != null && !competencia.isBlank()) {
                condiciones.add(cb.equal(raiz.get("competencia"), competencia));
            }
            if (tema != null && !tema.isBlank()) {
                condiciones.add(cb.equal(raiz.get("tema"), tema));
            }
            if (autorId != null) {
                condiciones.add(cb.equal(raiz.get("autorId"), autorId));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
        return preguntas.findAll(filtro, PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamano, 1), 100), Sort.by("id").descending()));
    }

    /**
     * Solicitud asíncrona: registra la operación como PENDING, publica el comando y devuelve el identificador. La respuesta
     * HTTP (202) no garantiza que la transición haya terminado: el resultado se consulta después.
     */
    public Operation solicitar(Long questionId, Comando comando, String solicitadoPor, String motivo) {
        obtener(questionId);
        Operation operacion = operaciones.save(new Operation(UUID.randomUUID().toString(), questionId, comando.tipo()));
        try {
            comandos.enviar(comando, questionId, operacion.getOperationId(), solicitadoPor, motivo);
        } catch (AmqpException e) {
            operacion.registrarResultado(OperationStatus.FAILED, "No se pudo publicar el comando en RabbitMQ");
            operaciones.save(operacion);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "RabbitMQ no está disponible; la solicitud no se aceptó");
        }
        return operacion;
    }

    public Operation operacion(String operationId) {
        return operaciones.findById(operationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe la operación " + operationId));
    }

    /** Lo llama el listener cuando lifecycle-service publica el resultado de una operación. */
    public void registrarResultado(String operationId, String status, String mensaje) {
        operaciones.findById(operationId).ifPresentOrElse(o -> {
            o.registrarResultado(OperationStatus.valueOf(status), mensaje);
            operaciones.save(o);
        }, () -> log.warn("Resultado de una operación desconocida: {}", operationId));
    }

    /** Lo llama el listener de `question.state-changed`: lifecycle es la autoridad, aquí solo queda una copia. */
    public void actualizarCopiaDelEstado(Long questionId, String estado) {
        if (questionId == null || estado == null) {
            return;
        }
        preguntas.findById(questionId).ifPresent(q -> {
            q.setEstado(EstadoPregunta.valueOf(estado));
            preguntas.save(q);
        });
    }

    /** Solo un usuario con el rol AUTOR puede crear o corregir preguntas (se consulta a user-service). */
    private void exigirAutor(Long autorId) {
        UsuarioInfo usuario = usuarios.buscar(autorId).orElse(null);
        if (usuario == null || usuario.roles() == null || !usuario.roles().contains("AUTOR")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "El usuario " + autorId + " no es un autor: solo un autor puede crear preguntas");
        }
    }

    private void validar(PreguntaRequest req) {
        ResultadoValidacion resultado = validacion.validar(req);
        if (!resultado.valid()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La pregunta no es válida", resultado.errors());
        }
    }

    private void aplicar(Question q, PreguntaRequest r) {
        q.actualizarContenido(r.titulo(), r.contexto(), r.pregunta(), r.opciones(), r.respuestaCorrecta().trim().toUpperCase(),
                r.justificacion(), r.bibliografia(), r.competencia(), r.tema(), r.subtema(), r.dificultad());
    }
}

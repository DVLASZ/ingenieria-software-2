package co.edu.unicauca.saberpro.question.application;

import co.edu.unicauca.saberpro.question.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.question.domain.exception.ReglaNegocioException;
import co.edu.unicauca.saberpro.question.domain.model.Enunciado;
import co.edu.unicauca.saberpro.question.domain.model.EstadoPregunta;
import co.edu.unicauca.saberpro.question.domain.model.Justificacion;
import co.edu.unicauca.saberpro.question.domain.model.OpcionRespuesta;
import co.edu.unicauca.saberpro.question.domain.model.Pregunta;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.ConsultarPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.EvaluarPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.NotificacionPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.PreguntaRepositoryPort;
import co.edu.unicauca.saberpro.question.domain.ports.outbound.ValidacionJustificacionPort;
import java.util.List;
import java.util.UUID;

public class QuestionApplicationService implements CrearPreguntaUseCase, EvaluarPreguntaUseCase, ConsultarPreguntaUseCase {

    private final PreguntaRepositoryPort repositorio;
    private final NotificacionPort notificacion;
    private final ValidacionJustificacionPort validacion;

    public QuestionApplicationService(PreguntaRepositoryPort repositorio, NotificacionPort notificacion,
                                      ValidacionJustificacionPort validacion) {
        this.repositorio = repositorio;
        this.notificacion = notificacion;
        this.validacion = validacion;
    }

    @Override
    public Pregunta crear(CrearPreguntaComando comando) {
        ValidacionJustificacionPort.Resultado resultado = validacion.validar(comando.justificacion());
        if (!resultado.valida()) {
            throw new ReglaNegocioException(resultado.mensaje());
        }
        List<OpcionRespuesta> opciones = comando.opciones() == null ? List.of()
                : comando.opciones().stream().map(o -> new OpcionRespuesta(o.texto(), o.correcta())).toList();
        Pregunta pregunta = Pregunta.crear(
                new Enunciado(comando.enunciado()), new Justificacion(comando.justificacion()), opciones);
        return repositorio.save(pregunta);
    }

    @Override
    public SolicitudAceptada enviarARevision(String preguntaId) {
        Pregunta pregunta = consultar(preguntaId);
        pregunta.verificarEnvioARevision();
        String operationId = UUID.randomUUID().toString();
        notificacion.solicitarRevision(pregunta, operationId);
        return new SolicitudAceptada(operationId, pregunta.id());
    }

    @Override
    public void confirmarEstado(String preguntaId, EstadoPregunta nuevoEstado) {
        Pregunta pregunta = consultar(preguntaId);
        if (pregunta.estado() == nuevoEstado) {
            return;
        }
        if (nuevoEstado == EstadoPregunta.PENDIENTE_REVISION) {
            pregunta.pasarAPendienteRevision();
            repositorio.save(pregunta);
            return;
        }
        throw new ReglaNegocioException("No se puede pasar a " + nuevoEstado + " desde " + pregunta.estado() + ".");
    }

    @Override
    public Pregunta consultar(String id) {
        return repositorio.findById(id).orElseThrow(() -> new PreguntaNoEncontradaException(id));
    }

    @Override
    public List<Pregunta> listar() {
        return repositorio.findAll();
    }
}

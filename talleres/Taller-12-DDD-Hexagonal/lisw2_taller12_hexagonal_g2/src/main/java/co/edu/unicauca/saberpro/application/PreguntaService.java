package co.edu.unicauca.saberpro.application;

import co.edu.unicauca.saberpro.domain.exception.PreguntaNoEncontradaException;
import co.edu.unicauca.saberpro.domain.model.Enunciado;
import co.edu.unicauca.saberpro.domain.model.Justificacion;
import co.edu.unicauca.saberpro.domain.model.OpcionRespuesta;
import co.edu.unicauca.saberpro.domain.model.Pregunta;
import co.edu.unicauca.saberpro.domain.ports.inbound.ConsultarPreguntaUseCase;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaUseCase;
import co.edu.unicauca.saberpro.domain.ports.outbound.NotificadorPort;
import co.edu.unicauca.saberpro.domain.ports.outbound.PreguntaRepositoryPort;
import java.util.List;

public class PreguntaService implements CrearPreguntaUseCase, ConsultarPreguntaUseCase {

    private final PreguntaRepositoryPort repositorio;
    private final NotificadorPort notificador;

    public PreguntaService(PreguntaRepositoryPort repositorio, NotificadorPort notificador) {
        this.repositorio = repositorio;
        this.notificador = notificador;
    }

    @Override
    public Pregunta crear(CrearPreguntaComando comando) {
        List<OpcionRespuesta> opciones = comando.opciones() == null ? List.of()
                : comando.opciones().stream().map(o -> new OpcionRespuesta(o.texto(), o.correcta())).toList();
        Pregunta pregunta = Pregunta.crear(
                new Enunciado(comando.enunciado()), new Justificacion(comando.justificacion()), opciones);
        return repositorio.save(pregunta);
    }

    @Override
    public Pregunta enviarARevision(String id) {
        Pregunta pregunta = consultar(id);
        pregunta.enviarARevision();
        Pregunta guardada = repositorio.save(pregunta);
        notificador.preguntaEnviadaARevision(guardada);
        return guardada;
    }

    @Override
    public Pregunta consultar(String id) {
        return repositorio.findById(id).orElseThrow(() -> new PreguntaNoEncontradaException(id));
    }
}

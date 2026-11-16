package co.edu.unicauca.saberpro.question.api;

import co.edu.unicauca.saberpro.question.api.dto.CrearPreguntaRequest;
import co.edu.unicauca.saberpro.question.api.dto.PreguntaResponse;
import co.edu.unicauca.saberpro.question.api.dto.SolicitudAceptadaResponse;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.ConsultarPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaUseCase.CrearPreguntaComando;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.CrearPreguntaUseCase.OpcionComando;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.EvaluarPreguntaUseCase;
import co.edu.unicauca.saberpro.question.domain.ports.inbound.EvaluarPreguntaUseCase.SolicitudAceptada;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST. Solo conoce los puertos de entrada. */
@RestController
@RequestMapping("/api/preguntas")
public class QuestionRestController {

    private final CrearPreguntaUseCase crear;
    private final EvaluarPreguntaUseCase evaluar;
    private final ConsultarPreguntaUseCase consultar;

    public QuestionRestController(CrearPreguntaUseCase crear, EvaluarPreguntaUseCase evaluar,
                                  ConsultarPreguntaUseCase consultar) {
        this.crear = crear;
        this.evaluar = evaluar;
        this.consultar = consultar;
    }

    @GetMapping
    public List<PreguntaResponse> listar() {
        return consultar.listar().stream().map(PreguntaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public PreguntaResponse consultar(@PathVariable String id) {
        return PreguntaResponse.de(consultar.consultar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PreguntaResponse crear(@RequestBody CrearPreguntaRequest request) {
        List<OpcionComando> opciones = request.opciones() == null ? List.of()
                : request.opciones().stream().map(o -> new OpcionComando(o.texto(), o.correcta())).toList();
        return PreguntaResponse.de(crear.crear(
                new CrearPreguntaComando(request.enunciado(), request.justificacion(), opciones)));
    }

    @PostMapping("/{id}/enviar-revision")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SolicitudAceptadaResponse enviarARevision(@PathVariable String id) {
        SolicitudAceptada aceptada = evaluar.enviarARevision(id);
        return new SolicitudAceptadaResponse(aceptada.operationId(), aceptada.preguntaId(),
                "Solicitud de revisión enviada a RabbitMQ");
    }
}

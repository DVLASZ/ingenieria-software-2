package co.edu.unicauca.saberpro.infrastructure.adapter.in.web;

import co.edu.unicauca.saberpro.domain.ports.inbound.ConsultarPreguntaUseCase;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaUseCase;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaUseCase.CrearPreguntaComando;
import co.edu.unicauca.saberpro.domain.ports.inbound.CrearPreguntaUseCase.OpcionComando;
import co.edu.unicauca.saberpro.infrastructure.adapter.in.web.dto.CrearPreguntaRequest;
import co.edu.unicauca.saberpro.infrastructure.adapter.in.web.dto.PreguntaResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/preguntas")
public class PreguntaController {

    private final CrearPreguntaUseCase crear;
    private final ConsultarPreguntaUseCase consultar;

    public PreguntaController(CrearPreguntaUseCase crear, ConsultarPreguntaUseCase consultar) {
        this.crear = crear;
        this.consultar = consultar;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PreguntaResponse crear(@RequestBody CrearPreguntaRequest request) {
        var opciones = request.opciones() == null ? java.util.List.<OpcionComando>of()
                : request.opciones().stream().map(o -> new OpcionComando(o.texto(), o.correcta())).toList();
        return PreguntaResponse.de(crear.crear(
                new CrearPreguntaComando(request.enunciado(), request.justificacion(), opciones)));
    }

    @PostMapping("/{id}/enviar-revision")
    public PreguntaResponse enviarARevision(@PathVariable String id) {
        return PreguntaResponse.de(crear.enviarARevision(id));
    }

    @GetMapping("/{id}")
    public PreguntaResponse consultar(@PathVariable String id) {
        return PreguntaResponse.de(consultar.consultar(id));
    }
}

package co.edu.unicauca.simulation.controller;

import co.edu.unicauca.simulation.dto.Dtos.CatalogoDto;
import co.edu.unicauca.simulation.dto.Dtos.CrearSimulacroRequest;
import co.edu.unicauca.simulation.dto.Dtos.ResolverRequest;
import co.edu.unicauca.simulation.dto.Dtos.ResultadoDto;
import co.edu.unicauca.simulation.dto.Dtos.SimulacroDto;
import co.edu.unicauca.simulation.model.Simulacro;
import co.edu.unicauca.simulation.service.SimulationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/simulations")
public class SimulationController {

    private final SimulationService servicio;

    public SimulationController(SimulationService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/catalog")
    public List<CatalogoDto> catalogo() {
        return servicio.catalogo().stream().map(CatalogoDto::de).toList();
    }

    /** Arma un simulacro con las primeras N preguntas del catálogo (sin mostrar las respuestas correctas). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SimulacroDto crear(@Valid @RequestBody CrearSimulacroRequest request) {
        Simulacro s = servicio.crear(request.userId(), request.cantidad());
        return SimulacroDto.de(s, servicio.preguntasDe(s));
    }

    @PostMapping("/{id}/submit")
    public ResultadoDto resolver(@PathVariable Long id, @Valid @RequestBody ResolverRequest request) {
        Simulacro s = servicio.resolver(id, request.respuestas());
        return new ResultadoDto(s.getId(), s.getUserId(), s.getCorrectas(), s.getTotal(), s.getPuntaje());
    }
}

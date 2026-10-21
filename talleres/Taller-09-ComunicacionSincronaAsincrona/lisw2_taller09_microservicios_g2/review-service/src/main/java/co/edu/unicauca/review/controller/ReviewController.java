package co.edu.unicauca.review.controller;

import co.edu.unicauca.review.dto.Dtos.AsignarRequest;
import co.edu.unicauca.review.dto.Dtos.CasoDto;
import co.edu.unicauca.review.dto.Dtos.EvaluacionRequest;
import co.edu.unicauca.review.dto.Dtos.PendienteDto;
import co.edu.unicauca.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService servicio;

    public ReviewController(ReviewService servicio) {
        this.servicio = servicio;
    }

    /** El administrador asigna de 1 a 3 revisores a una pregunta pendiente de revisión. */
    @PostMapping("/assign")
    @ResponseStatus(HttpStatus.CREATED)
    public CasoDto asignar(@Valid @RequestBody AsignarRequest request) {
        return CasoDto.de(servicio.asignar(request.questionId(), request.reviewerIds(), request.assignedBy()));
    }

    /** Evaluación de un revisor: aprobar o rechazar (el rechazo exige observación). */
    @PostMapping("/{questionId}/evaluations")
    public CasoDto evaluar(@PathVariable Long questionId, @Valid @RequestBody EvaluacionRequest request) {
        return CasoDto.de(servicio.evaluar(questionId, request.reviewerId(), request.decision(), request.observation()));
    }

    /** Todas las rondas de la pregunta con sus asignaciones y observaciones (visibles para revisores y autor). */
    @GetMapping("/{questionId}")
    public List<CasoDto> rondas(@PathVariable Long questionId) {
        return servicio.rondasDe(questionId).stream().map(CasoDto::de).toList();
    }

    @GetMapping("/assigned")
    public List<PendienteDto> pendientes(@RequestParam Long reviewerId) {
        return servicio.pendientesDe(reviewerId).stream().map(PendienteDto::de).toList();
    }
}

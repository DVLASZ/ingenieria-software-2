package co.edu.unicauca.tracking.controller;

import co.edu.unicauca.tracking.dto.Dtos.ResumenDto;
import co.edu.unicauca.tracking.dto.Dtos.UsuarioDto;
import co.edu.unicauca.tracking.service.TrackingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tracking")
public class TrackingController {

    private final TrackingService servicio;

    public TrackingController(TrackingService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/users/{id}")
    public UsuarioDto usuario(@PathVariable Long id) {
        return UsuarioDto.de(servicio.deUsuario(id));
    }

    @GetMapping("/summary")
    public ResumenDto resumen() {
        return servicio.resumen();
    }
}

package co.edu.unicauca.notification.controller;

import co.edu.unicauca.notification.dto.NotificacionDto;
import co.edu.unicauca.notification.model.NotificationType;
import co.edu.unicauca.notification.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService servicio;

    public NotificationController(NotificationService servicio) {
        this.servicio = servicio;
    }

    /** Sirve para comprobar en Postman que el evento generó su notificación. */
    @GetMapping
    public List<NotificacionDto> listar(@RequestParam(required = false) Long destinatario,
                                        @RequestParam(required = false) NotificationType tipo) {
        return servicio.buscar(destinatario, tipo).stream().map(NotificacionDto::de).toList();
    }
}

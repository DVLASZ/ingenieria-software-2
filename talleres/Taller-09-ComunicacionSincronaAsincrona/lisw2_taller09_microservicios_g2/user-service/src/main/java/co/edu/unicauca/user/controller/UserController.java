package co.edu.unicauca.user.controller;

import co.edu.unicauca.user.dto.Dtos.UsuarioDto;
import co.edu.unicauca.user.model.Rol;
import co.edu.unicauca.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService servicio;

    public UserController(UserService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public UsuarioDto obtener(@PathVariable Long id) {
        return servicio.buscar(id).map(UsuarioDto::de)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe el usuario " + id));
    }

    /** Revisores elegibles: `GET /users?role=REVISOR&excluir={autorId}`. */
    @GetMapping
    public List<UsuarioDto> listar(@RequestParam Rol role, @RequestParam(required = false) Long excluir) {
        return servicio.porRol(role, excluir).stream().map(UsuarioDto::de).toList();
    }
}

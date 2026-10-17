package co.edu.unicauca.user.controller;

import co.edu.unicauca.user.dto.Dtos.LoginRequest;
import co.edu.unicauca.user.dto.Dtos.LoginResponse;
import co.edu.unicauca.user.dto.Dtos.UsuarioDto;
import co.edu.unicauca.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Autenticación síncrona: sin JWT en esta versión, el login devuelve un token simple. */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService servicio;

    public AuthController(UserService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return servicio.login(request.username(), request.password())
                .map(s -> ResponseEntity.ok(new LoginResponse(true, "Autenticación exitosa", UsuarioDto.de(s.usuario()), s.token())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(false, "Credenciales incorrectas", null, null)));
    }
}

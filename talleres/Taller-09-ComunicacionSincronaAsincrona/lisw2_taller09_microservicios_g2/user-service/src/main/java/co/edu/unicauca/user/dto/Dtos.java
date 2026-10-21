package co.edu.unicauca.user.dto;

import co.edu.unicauca.user.model.Rol;
import co.edu.unicauca.user.model.Usuario;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;
import java.util.TreeSet;

/** Contratos REST del servicio de usuarios. */
public final class Dtos {

    private Dtos() {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record UsuarioDto(Long id, String username, String nombreCompleto, String email, Set<Rol> roles) {
        public static UsuarioDto de(Usuario u) {
            return new UsuarioDto(u.getId(), u.getUsername(), u.getNombreCompleto(), u.getEmail(), new TreeSet<>(u.getRoles()));
        }
    }

    public record LoginResponse(boolean authenticated, String message, UsuarioDto user, String token) {
    }
}

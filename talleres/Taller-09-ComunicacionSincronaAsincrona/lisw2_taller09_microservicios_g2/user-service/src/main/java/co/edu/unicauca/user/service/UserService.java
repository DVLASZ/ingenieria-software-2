package co.edu.unicauca.user.service;

import co.edu.unicauca.user.messaging.AuthEventPublisher;
import co.edu.unicauca.user.model.Rol;
import co.edu.unicauca.user.model.Usuario;
import co.edu.unicauca.user.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UsuarioRepository usuarios;
    private final AuthEventPublisher eventos;

    public UserService(UsuarioRepository usuarios, AuthEventPublisher eventos) {
        this.usuarios = usuarios;
        this.eventos = eventos;
    }

    /** Resultado del login: el usuario y el token simple, o vacío si las credenciales no coinciden. */
    public record Sesion(Usuario usuario, String token) {
    }

    public Optional<Sesion> login(String username, String password) {
        Optional<Usuario> encontrado = usuarios.findByUsername(username).filter(u -> u.claveCoincide(password));
        if (encontrado.isEmpty()) {
            eventos.loginFallido(username);
            return Optional.empty();
        }
        Usuario u = encontrado.get();
        eventos.loginExitoso(u.getId(), u.getUsername());
        return Optional.of(new Sesion(u, UUID.randomUUID().toString()));
    }

    public Optional<Usuario> buscar(Long id) {
        return usuarios.findById(id);
    }

    /** Usuarios con un rol, sin incluir al excluido (por ejemplo, el autor de la pregunta). */
    public List<Usuario> porRol(Rol rol, Long excluirId) {
        return usuarios.findByRol(rol).stream().filter(u -> !u.getId().equals(excluirId)).toList();
    }
}

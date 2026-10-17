package co.edu.unicauca.user.config;

import co.edu.unicauca.user.model.Rol;
import co.edu.unicauca.user.model.Usuario;
import co.edu.unicauca.user.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/** Usuarios precargados (los mismos del proyecto). La contraseña de demostración es `clave123` para todos. */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner cargarUsuarios(UsuarioRepository repo) {
        return args -> {
            if (repo.count() > 0) {
                return;
            }
            repo.save(new Usuario("admin", "clave123", "Administrador del banco", "admin@bancopreguntas.test", Set.of(Rol.ADMINISTRADOR)));
            repo.save(new Usuario("autor1", "clave123", "Autor Uno", "autor1@bancopreguntas.test", Set.of(Rol.AUTOR)));
            repo.save(new Usuario("autor2", "clave123", "Autor Dos", "autor2@bancopreguntas.test", Set.of(Rol.AUTOR)));
            repo.save(new Usuario("revisor1", "clave123", "Revisor Uno", "revisor1@bancopreguntas.test", Set.of(Rol.REVISOR)));
            repo.save(new Usuario("revisor2", "clave123", "Revisor Dos", "revisor2@bancopreguntas.test", Set.of(Rol.REVISOR)));
            repo.save(new Usuario("revisor3", "clave123", "Revisor Tres", "revisor3@bancopreguntas.test", Set.of(Rol.REVISOR)));
            repo.save(new Usuario("revisor4", "clave123", "Revisor Cuatro", "revisor4@bancopreguntas.test", Set.of(Rol.REVISOR)));
        };
    }
}

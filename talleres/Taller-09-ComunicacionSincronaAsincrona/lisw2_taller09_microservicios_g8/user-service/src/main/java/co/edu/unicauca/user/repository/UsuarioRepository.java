package co.edu.unicauca.user.repository;

import co.edu.unicauca.user.model.Rol;
import co.edu.unicauca.user.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    @Query("select distinct u from Usuario u join u.roles r where r = :rol order by u.id")
    List<Usuario> findByRol(@Param("rol") Rol rol);
}

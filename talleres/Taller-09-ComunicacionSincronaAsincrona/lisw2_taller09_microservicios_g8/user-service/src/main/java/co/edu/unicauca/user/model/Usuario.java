package co.edu.unicauca.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    /** Hash SHA-256 de `saberpro:<usuario>:<clave>`; nunca se guarda la clave. */
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Rol> roles = new HashSet<>();

    protected Usuario() {
    }

    public Usuario(String username, String password, String nombreCompleto, String email, Set<Rol> roles) {
        this.username = username;
        this.passwordHash = hash(username, password);
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.roles = new HashSet<>(roles);
    }

    public boolean tieneRol(Rol rol) {
        return roles.contains(rol);
    }

    public boolean claveCoincide(String intento) {
        return intento != null && passwordHash.equals(hash(username, intento));
    }

    static String hash(String usuario, String clave) {
        try {
            byte[] resumen = MessageDigest.getInstance("SHA-256").digest(("saberpro:" + usuario + ":" + clave).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumen);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public Set<Rol> getRoles() {
        return roles;
    }
}

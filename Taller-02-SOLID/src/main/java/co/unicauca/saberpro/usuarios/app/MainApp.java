package co.unicauca.saberpro.usuarios.app;

import co.unicauca.saberpro.usuarios.domain.access.IUserRepository;
import co.unicauca.saberpro.usuarios.domain.access.UserRepositoryFactory;
import co.unicauca.saberpro.usuarios.domain.menu.MenuProviderRegistry;
import co.unicauca.saberpro.usuarios.domain.security.IPasswordHasher;
import co.unicauca.saberpro.usuarios.domain.security.PasswordHasherFactory;
import co.unicauca.saberpro.usuarios.domain.service.UserService;
import co.unicauca.saberpro.usuarios.domain.validation.IPasswordPolicy;
import co.unicauca.saberpro.usuarios.domain.validation.PasswordPolicyFactory;
import co.unicauca.saberpro.usuarios.ui.LoginFrame;

import javax.swing.*;

/**
 * Punto de composicion (composition root) de la aplicacion: el UNICO lugar
 * donde se pide a cada fabrica la implementacion concreta que se va a usar
 * (SQLite como {@link IUserRepository}, Argon2 como {@link IPasswordHasher},
 * etc.). Replica la forma en que {@code ClientMain} usa {@code Factory} en
 * el ejemplo 5 de Inversion de Dependencias visto en la teoria: todo lo
 * demas en el proyecto (servicio, UI) solo conoce interfaces.
 */
public final class MainApp {

    private MainApp() {
    }

    public static void main(String[] args) {
        // Le pedimos a cada fabrica la implementacion por defecto.
        IUserRepository repository = UserRepositoryFactory.getInstance().getRepository("default");
        IPasswordHasher passwordHasher = PasswordHasherFactory.getInstance().getHasher("default");
        IPasswordPolicy passwordPolicy = PasswordPolicyFactory.getInstance().getPolicy("default");

        UserService userService = new UserService(repository, passwordHasher, passwordPolicy);
        MenuProviderRegistry menuProviderRegistry = MenuProviderRegistry.withDefaultProviders();

        SwingUtilities.invokeLater(() -> new LoginFrame(userService, menuProviderRegistry).setVisible(true));
    }
}

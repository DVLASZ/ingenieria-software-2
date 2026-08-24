package co.unicauca.saberpro.usuarios.ui;

import co.unicauca.saberpro.usuarios.domain.User;
import co.unicauca.saberpro.usuarios.domain.menu.MenuProviderRegistry;
import co.unicauca.saberpro.usuarios.domain.service.UserService;
import co.unicauca.saberpro.usuarios.domain.service.exception.AuthenticationException;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla inicial de la aplicacion (RF-02): autenticacion por usuario y
 * contrasena. Al autenticar con exito, carga el tablero correspondiente al
 * rol del usuario.
 */
public class LoginFrame extends JFrame {

    private final UserService userService;
    private final MenuProviderRegistry menuProviderRegistry;

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    public LoginFrame(UserService userService, MenuProviderRegistry menuProviderRegistry) {
        super("Banco de Preguntas Saber Pro - Inicio de sesion");
        this.userService = userService;
        this.menuProviderRegistry = menuProviderRegistry;
        buildUi();
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        add(new JLabel("Usuario:"), c);
        c.gridx = 1;
        add(usernameField, c);

        c.gridx = 0; c.gridy = 1;
        add(new JLabel("Contrasena:"), c);
        c.gridx = 1;
        add(passwordField, c);

        JButton loginButton = new JButton("Iniciar sesion");
        loginButton.addActionListener(e -> onLogin());
        JButton registerButton = new JButton("Registrarse");
        registerButton.addActionListener(e -> new RegisterFrame(userService).setVisible(true));

        JPanel buttons = new JPanel();
        buttons.add(loginButton);
        buttons.add(registerButton);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        add(buttons, c);

        pack();
        setLocationRelativeTo(null);
    }

    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        try {
            User user = userService.authenticate(username, password);
            new DashboardFrame(user, menuProviderRegistry).setVisible(true);
            passwordField.setText("");
        } catch (AuthenticationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No fue posible iniciar sesion",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

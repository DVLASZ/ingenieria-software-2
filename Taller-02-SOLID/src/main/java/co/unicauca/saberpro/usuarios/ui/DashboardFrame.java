package co.unicauca.saberpro.usuarios.ui;

import co.unicauca.saberpro.usuarios.domain.User;
import co.unicauca.saberpro.usuarios.domain.menu.MenuProviderRegistry;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Tablero mostrado tras iniciar sesion. Las opciones que se listan
 * dependen del rol del usuario autenticado y se obtienen a traves de
 * {@link MenuProviderRegistry}, sin que esta clase necesite saber cuantos
 * roles existen ni como se arma cada menu (OCP).
 */
public class DashboardFrame extends JFrame {

    public DashboardFrame(User user, MenuProviderRegistry menuProviderRegistry) {
        super("Banco de Preguntas Saber Pro - Tablero (" + user.getRole().getDisplayName() + ")");
        buildUi(user, menuProviderRegistry);
    }

    private void buildUi(User user, MenuProviderRegistry menuProviderRegistry) {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel welcomeLabel = new JLabel(
                "Bienvenido(a), " + user.getFullName() + "  —  Rol: " + user.getRole().getDisplayName());
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(welcomeLabel, BorderLayout.NORTH);

        List<String> options = menuProviderRegistry.menuOptionsFor(user.getRole());
        DefaultListModel<String> listModel = new DefaultListModel<>();
        options.forEach(listModel::addElement);
        JList<String> menuList = new JList<>(listModel);
        menuList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(menuList), BorderLayout.CENTER);

        setSize(420, 320);
        setLocationRelativeTo(null);
    }
}

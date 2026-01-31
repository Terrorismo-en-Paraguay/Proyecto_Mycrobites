package org.example.calendario_app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        String savedEmail = org.example.calendario_app.util.PrefsManager.getEmail();
        String savedPassword = org.example.calendario_app.util.PrefsManager.getPassword();
        boolean isLoggedIn = false;

        if (savedEmail != null && savedPassword != null) {
            org.example.calendario_app.dao.UsuarioDAO usuarioDAO = new org.example.calendario_app.dao.UsuarioDAO(
                    new org.example.calendario_app.dao.impl.UsuarioDAOImpl());
            org.example.calendario_app.model.Usuario usuario = usuarioDAO.iniciarSesion(savedEmail, savedPassword);

            if (usuario != null) {
                org.example.calendario_app.dao.ClienteDAO clienteDAO = new org.example.calendario_app.dao.ClienteDAO(
                        new org.example.calendario_app.dao.impl.ClienteDAOImpl());
                org.example.calendario_app.model.Cliente cliente = clienteDAO.obtenerPorId(usuario.getId_cliente());

                org.example.calendario_app.util.Session.getInstance().setUsuario(usuario);
                org.example.calendario_app.util.Session.getInstance().setCliente(cliente);

                FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("calendar-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 1280, 800);
                stage.setScene(scene);

                // Obtener pantalla actual para maximizar correctamente si es necesario o
                // centrar
                javafx.collections.ObservableList<javafx.stage.Screen> screens = javafx.stage.Screen
                        .getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
                if (!screens.isEmpty()) {
                    javafx.stage.Screen screen = screens.get(0);
                    javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
                    stage.setX(bounds.getMinX());
                    stage.setY(bounds.getMinY());
                    stage.setWidth(bounds.getWidth());
                    stage.setHeight(bounds.getHeight());
                }

                stage.setMaximized(true);
                stage.show();
                isLoggedIn = true;
            }
        }

        if (!isLoggedIn) {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 320, 240);
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}

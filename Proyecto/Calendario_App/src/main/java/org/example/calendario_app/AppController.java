package org.example.calendario_app;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AppController {
    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    public void onLoginButtonClick() {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error de validación", "Por favor, ingrese correo y contraseña.");
            return;
        }

        // Simulación de autenticación simplificada
        if ("admin".equals(email) && "1234".equals(password)) {
            try {
                // Cargar la vista del calendario
                javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("calendar-view.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 1280, 800); // Tamaño más grande
                                                                                                 // para el calendario

                // Obtener el stage actual y cambiar la escena
                javafx.stage.Stage stage = (javafx.stage.Stage) btnLogin.getScene().getWindow();
                stage.setScene(scene);
                stage.setMaximized(true); // Asegurar que esté maximizado
                stage.centerOnScreen();
            } catch (java.io.IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la pantalla del calendario.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error de inicio de sesión", "Correo o contraseña incorrectos.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

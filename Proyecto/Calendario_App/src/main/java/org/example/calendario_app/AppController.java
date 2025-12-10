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
    private TextField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    protected void onLoginButtonClick() {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error de validación", "Por favor, ingrese correo y contraseña.");
            return;
        }

        // Simulación de autenticación
        if ("admin".equals(email) && "1234".equals(password)) {
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Inicio de sesión correcto.");
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

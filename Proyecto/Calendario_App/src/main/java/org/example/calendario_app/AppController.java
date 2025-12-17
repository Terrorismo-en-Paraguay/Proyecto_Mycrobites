package org.example.calendario_app;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.calendario_app.dao.UsuarioDAO;
import org.example.calendario_app.dao.impl.UsuarioDAOImpl;

public class AppController {
    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Label lblHeader;

    @FXML
    private Button btnToggleLogin;

    @FXML
    private Button btnToggleRegister;

    @FXML
    private HBox boxForgotPassword;

    @FXML
    private VBox registerFieldsContainer;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtSurname;

    private UsuarioDAOImpl udi = new UsuarioDAOImpl();

    private UsuarioDAO usuarioDAO = new UsuarioDAO(udi);

    private boolean isLoginMode = true;

    @FXML
    public void onToggleLogin() {
        if (!isLoginMode) {
            isLoginMode = true;
            updateUI();
        }
    }

    @FXML
    public void onToggleRegister() {
        if (isLoginMode) {
            isLoginMode = false;
            updateUI();
        }
    }

    private void updateUI() {
        if (isLoginMode) {
            // Modo Login
            lblHeader.setText("Accede a tu cuenta");
            btnLogin.setText("Iniciar Sesión");
            boxForgotPassword.setVisible(true);
            boxForgotPassword.setManaged(true);
            registerFieldsContainer.setVisible(false);
            registerFieldsContainer.setManaged(false);

            // Estilos botones
            btnToggleLogin.getStyleClass().add("active");
            btnToggleRegister.getStyleClass().remove("active");
        } else {
            // Modo Registro
            lblHeader.setText("Regístrate");
            btnLogin.setText("Registrarse");
            boxForgotPassword.setVisible(false);
            boxForgotPassword.setManaged(false);
            registerFieldsContainer.setVisible(true);
            registerFieldsContainer.setManaged(true);

            // Estilos botones
            btnToggleRegister.getStyleClass().add("active");
            btnToggleLogin.getStyleClass().remove("active");
        }

        // Limpiar campos al cambiar
        txtEmail.clear();
        txtPassword.clear();
        txtName.clear();
        txtSurname.clear();
    }

    @FXML
    public void onLoginButtonClick() {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error de validación", "Por favor, ingrese correo y contraseña.");
            return;
        }

        if (isLoginMode) {
            // Lógica de Login con Base de Datos
            if (usuarioDAO.iniciarSesion(email, password)) {
                loadCalendar();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error de inicio de sesión", "Correo o contraseña incorrectos.");
            }
        } else {
            // Lógica de Registro
            String name = txtName.getText();
            String surname = txtSurname.getText();

            if (name.isEmpty() || surname.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error de validación",
                        "Por favor, complete todos los campos (Nombre, Apellidos, Correo, Contraseña).");
                return;
            }

            // Simulación Registro - Aquí iría la llamada al servicio de registro
            showAlert(Alert.AlertType.INFORMATION, "Registro Exitoso",
                    String.format("Usuario %s %s registrado correctamente.\n(Simulación)", name, surname));

            // Volver a login
            onToggleLogin();
        }
    }

    private void loadCalendar() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("calendar-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 1280, 800);

            javafx.stage.Stage stage = (javafx.stage.Stage) btnLogin.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la pantalla del calendario.");
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

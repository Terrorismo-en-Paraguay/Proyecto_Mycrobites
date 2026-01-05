package org.example.calendario_app;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.calendario_app.dao.ClienteDAO;
import org.example.calendario_app.dao.UsuarioDAO;
import org.example.calendario_app.dao.impl.ClienteDAOImpl;
import org.example.calendario_app.dao.impl.UsuarioDAOImpl;
import org.example.calendario_app.model.Cliente;
import org.example.calendario_app.model.Usuario;
import org.example.calendario_app.util.Session;

import java.time.LocalDate;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.collections.ObservableList;

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

    private UsuarioDAO usuarioDAO;
    private ClienteDAO clienteDAO;

    private boolean isLoginMode = true;

    public AppController() {
        this.usuarioDAO = new UsuarioDAO(new UsuarioDAOImpl());
        this.clienteDAO = new ClienteDAO(new ClienteDAOImpl());
    }

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

        if (isLoginMode) {
            // Lógica de Login
            if (email.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error de validación", "Por favor, ingrese correo y contraseña.");
                return;
            }

            Usuario usuario = usuarioDAO.iniciarSesion(email, password);
            if (usuario != null) {
                // Fetch associated Client
                Cliente cliente = clienteDAO.obtenerPorId(usuario.getId_cliente());

                // Store in Session
                Session.getInstance().setUsuario(usuario);
                Session.getInstance().setCliente(cliente);

                loadCalendar();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error de inicio de sesión", "Correo o contraseña incorrectos.");
            }
        } else {
            // Lógica de Registro
            String name = txtName.getText();
            String surname = txtSurname.getText();

            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error de validación",
                        "Por favor, complete todos los campos (Nombre, Apellidos, Correo, Contraseña).");
                return;
            }

            try {
                // 1. Crear Cliente
                Cliente nuevoCliente = new Cliente(name, surname, LocalDate.now());
                int idCliente = clienteDAO.registrar(nuevoCliente);

                if (idCliente != -1) {
                    // 2. Crear Usuario vinculado al Cliente
                    Usuario nuevoUsuario = new Usuario(idCliente, email, password, "USER");
                    boolean registroExitoso = usuarioDAO.registrar(nuevoUsuario);

                    if (registroExitoso) {
                        showAlert(Alert.AlertType.INFORMATION, "Registro Exitoso",
                                "Usuario registrado correctamente. Ahora puede iniciar sesión.");
                        onToggleLogin();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error de Registro", "No se pudo crear el usuario.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error de Registro", "No se pudo crear el cliente.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error de Sistema",
                        "Ocurrió un error durante el registro: " + e.getMessage());
            }
        }
    }

    private void loadCalendar() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("calendar-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 1280, 800);

            javafx.stage.Stage stage = (javafx.stage.Stage) btnLogin.getScene().getWindow();
            stage.setScene(scene);

            // Get current screen
            ObservableList<Screen> screens = Screen.getScreensForRectangle(stage.getX(), stage.getY(),
                    stage.getWidth(), stage.getHeight());
            if (!screens.isEmpty()) {
                Screen screen = screens.get(0);
                Rectangle2D bounds = screen.getVisualBounds();
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth());
                stage.setHeight(bounds.getHeight());
            }
            stage.setMaximized(true);
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

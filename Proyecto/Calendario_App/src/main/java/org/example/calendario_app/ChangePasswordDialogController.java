package org.example.calendario_app;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class ChangePasswordDialogController {

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    private Stage dialogStage;
    private boolean saveClicked = false;
    private String newPassword = null;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public String getNewPassword() {
        return newPassword;
    }

    @FXML
    private void handleSave() {
        String pass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (pass == null || pass.isEmpty()) {
            errorLabel.setText("La contraseña no puede estar vacía.");
            return;
        }

        if (!pass.equals(confirm)) {
            errorLabel.setText("Las contraseñas no coinciden.");
            return;
        }

        this.newPassword = pass;
        this.saveClicked = true;
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}

package org.example.calendario_app;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public class LabelDialogController {

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<String> colorComboBox;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    private Stage dialogStage;
    private boolean saveClicked = false;
    private String labelName;
    private String labelColorClass;

    private final Map<String, String> colorMap = new HashMap<>();

    @FXML
    public void initialize() {
        colorMap.put("Morado", "purple");
        colorMap.put("Verde", "green");
        colorMap.put("Naranja", "orange");
        colorMap.put("Rojo", "red");

        colorComboBox.getItems().addAll(colorMap.keySet());
        colorComboBox.getSelectionModel().selectFirst();

        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> dialogStage.close());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public String getLabelName() {
        return labelName;
    }

    public String getLabelColorClass() {
        return labelColorClass;
    }

    private void handleSave() {
        if (isValid()) {
            labelName = nameField.getText();
            String selectedColorName = colorComboBox.getValue();
            labelColorClass = colorMap.get(selectedColorName);
            saveClicked = true;
            dialogStage.close();
        }
    }

    private boolean isValid() {
        String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().length() == 0) {
            errorMessage += "Nombre inválido!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show alert or simple print
            System.out.println(errorMessage);
            return false;
        }
    }
}

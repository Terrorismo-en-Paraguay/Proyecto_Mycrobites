package org.example.calendario_app;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EventDialogController {

    @FXML
    private TextField titleField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private Event event;
    private boolean saveClicked = false;
    private Stage dialogStage;

    @FXML
    private void initialize() {
        // Validation: verify title and date are not empty
        saveButton.disableProperty().bind(
                titleField.textProperty().isEmpty().or(datePicker.valueProperty().isNull()));

        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public Event getEvent() {
        return event;
    }

    private void handleSave() {
        if (isValid()) {
            event = new Event(
                    titleField.getText(),
                    datePicker.getValue(),
                    descriptionArea.getText());
            saveClicked = true;
            dialogStage.close();
        }
    }

    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isValid() {
        // Basic validation already handled by binding, logic can be extended here
        return true;
    }
}

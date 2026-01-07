package org.example.calendario_app;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javafx.scene.control.ComboBox;
import org.example.calendario_app.model.Evento;
import org.example.calendario_app.model.Etiqueta;
import javafx.util.StringConverter;
import java.util.List;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.stream.IntStream;
import javafx.collections.FXCollections;

public class EventDialogController {

    @FXML
    private TextField titleField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<Etiqueta> labelComboBox;

    @FXML
    private ComboBox<Integer> startHourCombo;
    @FXML
    private ComboBox<Integer> startMinuteCombo;
    @FXML
    private ComboBox<Integer> endHourCombo;
    @FXML
    private ComboBox<Integer> endMinuteCombo;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private Evento event;
    private boolean saveClicked = false;
    private Stage dialogStage;

    @FXML
    private void initialize() {
        // Populate Time Combos
        List<Integer> hours = IntStream.range(0, 24).boxed().toList();
        List<Integer> minutes = IntStream.range(0, 60).filter(i -> i % 5 == 0).boxed().toList();

        startHourCombo.setItems(FXCollections.observableArrayList(hours));
        startMinuteCombo.setItems(FXCollections.observableArrayList(minutes));
        endHourCombo.setItems(FXCollections.observableArrayList(hours));
        endMinuteCombo.setItems(FXCollections.observableArrayList(minutes));

        // Defaults
        startHourCombo.getSelectionModel().select(Integer.valueOf(9));
        startMinuteCombo.getSelectionModel().select(Integer.valueOf(0));
        endHourCombo.getSelectionModel().select(Integer.valueOf(10));
        endMinuteCombo.getSelectionModel().select(Integer.valueOf(0));

        // Validation: verify title and date are not empty
        saveButton.disableProperty().bind(
                titleField.textProperty().isEmpty().or(datePicker.valueProperty().isNull()));

        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());

        // Configure ComboBox to show label names (existing code)
        labelComboBox.setConverter(new StringConverter<Etiqueta>() {
            @Override
            public String toString(Etiqueta object) {
                return object != null ? object.getNombre() : "";
            }

            @Override
            public Etiqueta fromString(String string) {
                return null;
            }
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public Evento getEvento() {
        return event;
    }

    public void setLabels(List<Etiqueta> labels) {
        labelComboBox.getItems().setAll(labels);
        if (!labels.isEmpty()) {
            labelComboBox.getSelectionModel().selectFirst();
        }
    }

    private void handleSave() {
        if (isValid()) {
            Etiqueta selectedLabel = labelComboBox.getValue();
            Integer labelId = selectedLabel != null ? selectedLabel.getId() : null;

            // Times
            int startH = startHourCombo.getValue() != null ? startHourCombo.getValue() : 9;
            int startM = startMinuteCombo.getValue() != null ? startMinuteCombo.getValue() : 0;
            int endH = endHourCombo.getValue() != null ? endHourCombo.getValue() : 10;
            int endM = endMinuteCombo.getValue() != null ? endMinuteCombo.getValue() : 0;

            LocalDateTime startDateTime = datePicker.getValue().atTime(startH, startM);
            LocalDateTime endDateTime = datePicker.getValue().atTime(endH, endM);

            // Note: client ID will be set by the main controller
            event = new Evento(
                    titleField.getText(),
                    descriptionArea.getText(),
                    startDateTime,
                    endDateTime,
                    "", // Location default empty
                    0, // Creator temporarily 0
                    labelId);
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

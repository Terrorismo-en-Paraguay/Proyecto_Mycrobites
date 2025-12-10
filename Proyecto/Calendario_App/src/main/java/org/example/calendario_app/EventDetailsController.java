package org.example.calendario_app;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class EventDetailsController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Button closeButton;

    private Stage dialogStage;

    @FXML
    private void initialize() {
        closeButton.setOnAction(e -> handleClose());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setEvent(Event event) {
        if (event != null) {
            titleLabel.setText(event.getTitle());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy",
                    Locale.forLanguageTag("es-ES"));
            String formattedDate = event.getDate().format(formatter);
            formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
            dateLabel.setText(formattedDate);

            descriptionLabel.setText(event.getDescription() != null && !event.getDescription().isEmpty()
                    ? event.getDescription()
                    : "Sin descripción.");
        }
    }

    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}

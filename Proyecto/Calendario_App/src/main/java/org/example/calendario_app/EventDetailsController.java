package org.example.calendario_app;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.example.calendario_app.model.Evento;

public class EventDetailsController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label timeLabel;

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

    public void setEvento(Evento event) {
        if (event != null) {
            titleLabel.setText(event.getTitulo());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy",
                    Locale.forLanguageTag("es-ES"));
            String formattedDate = event.getFecha().format(formatter);
            formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
            formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
            dateLabel.setText(formattedDate);

            // Format Time Range
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            String startStr = event.getFecha_inicio().format(timeFormatter);
            String endStr = event.getFecha_fin().format(timeFormatter);
            timeLabel.setText(startStr + " - " + endStr);

            descriptionLabel.setText(event.getDescripcion() != null && !event.getDescripcion().isEmpty()
                    ? event.getDescripcion()
                    : "Sin descripción.");
        }
    }

    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}

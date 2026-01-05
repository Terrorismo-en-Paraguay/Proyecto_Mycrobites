package org.example.calendario_app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import javafx.geometry.Side;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.example.calendario_app.util.Session;

public class CalendarController {

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label monthYearLabel;

    @FXML
    private Button btnPrev;

    @FXML
    private Button btnNext;

    @FXML
    private Label miniMonthLabel;

    @FXML
    private Label userInitialLabel;

    @FXML
    private StackPane userIconContainer;

    @FXML
    private GridPane miniCalendarGrid;

    @FXML
    private Button btnCreateEvent;

    private YearMonth currentYearMonth;
    private final List<Event> events = new ArrayList<>();

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();

        // Set user initial
        if (Session.getInstance().getCliente() != null) {
            String name = Session.getInstance().getCliente().getNombre();
            if (name != null && !name.isEmpty()) {
                String initial = name.substring(0, 1).toUpperCase();
                userInitialLabel.setText(initial);

                // Configurar menú de usuario
                setupUserMenu(name);
            }
        }

        drawCalendar();
        drawMiniCalendar();

        btnPrev.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            drawCalendar();
            drawMiniCalendar();
        });

        btnNext.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            drawCalendar();
            drawMiniCalendar();
        });

        btnCreateEvent.setOnAction(e -> openCreateEventDialog());
    }

    private void openCreateEventDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("event-dialog.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Crear Evento");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnCreateEvent.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            EventDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                events.add(controller.getEvent());
                drawCalendar(); // Refresh to show new event
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawCalendar() {
        // Update Header Label
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-ES"));
        String formattedDate = currentYearMonth.format(formatter);
        // Capitalize first letter
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        monthYearLabel.setText(formattedDate);

        // Clear grid but keep headers (Row 0)
        // A simple way is to remove nodes that are in row > 0
        calendarGrid.getChildren().removeIf(node -> {
            Integer rowIndex = GridPane.getRowIndex(node);
            return rowIndex != null && rowIndex > 0;
        });

        // Calculate days
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        DayOfWeek firstDayOfWeek = firstOfMonth.getDayOfWeek();
        int dayOfWeekValue = firstDayOfWeek.getValue(); // Monday = 1, Sunday = 7

        // We want Monday as start, so offset is dayOfWeekValue - 1
        // Example: If 1st is Monday (1), offset is 0. If Tuesday (2), offset is 1.
        int offset = dayOfWeekValue - 1;

        LocalDate dateIterator = firstOfMonth.minusDays(offset);

        // Draw 5 or 6 rows (mostly 6 covers all cases)
        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                VBox cell = new VBox();
                cell.setSpacing(2);

                // Style
                boolean isCurrentMonth = dateIterator.getMonth().equals(currentYearMonth.getMonth());
                if (isCurrentMonth) {
                    cell.getStyleClass().add("day-cell");
                } else {
                    cell.getStyleClass().add("day-cell-dimmed");
                }

                // Day Label
                Label dayLabel = new Label(String.valueOf(dateIterator.getDayOfMonth()));
                dayLabel.getStyleClass().add("day-label");
                cell.getChildren().add(dayLabel);

                // Add User Created Events
                for (Event event : events) {
                    if (event.getDate().equals(dateIterator)) {
                        addEventLabel(cell, event);
                    }
                }

                // Simulate Events (Only for current month for simplicity, or random)
                // if (isCurrentMonth && random.nextDouble() < 0.2) {
                // addRandomEvent(cell);
                // }

                calendarGrid.add(cell, col, row);

                dateIterator = dateIterator.plusDays(1);
            }
        }
    }

    private void openEventDetailsDialog(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("event-details-view.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Detalles del Evento");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnCreateEvent.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            EventDetailsController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setEvent(event);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addEventLabel(VBox cell, Event event) {
        Label eventLabel = new Label("• " + event.getTitle());
        // Simple random color for variety or specific based on type
        // defaulting to purple for user events for now
        eventLabel.getStyleClass().add("event-label-purple");
        eventLabel.setWrapText(true);

        // Add click listener
        eventLabel.setOnMouseClicked(e -> {
            e.consume(); // Prevent bubbling if needed
            openEventDetailsDialog(event);
        });

        // Change cursor to indicate clickability (already in CSS for icon-btn,
        // beneficial here too)
        eventLabel.setStyle("-fx-cursor: hand;");

        cell.getChildren().add(eventLabel);
    }

    private void drawMiniCalendar() {
        // Update Header Label
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-ES"));
        String formattedDate = currentYearMonth.format(formatter);
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        miniMonthLabel.setText(formattedDate);

        // Clear grid but keep headers
        miniCalendarGrid.getChildren().removeIf(node -> {
            Integer rowIndex = GridPane.getRowIndex(node);
            return rowIndex != null && rowIndex > 0;
        });

        // Calculate days (same as main calendar)
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue();
        int offset = dayOfWeekValue - 1;
        LocalDate dateIterator = firstOfMonth.minusDays(offset);

        // Draw rows
        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                Label dayLabel = new Label(String.valueOf(dateIterator.getDayOfMonth()));
                boolean isCurrentMonth = dateIterator.getMonth().equals(currentYearMonth.getMonth());

                if (isCurrentMonth) {
                    dayLabel.setStyle("-fx-text-fill: white; -fx-padding: 3;");
                    // Highlight today or random specific day logic could go here
                } else {
                    dayLabel.setStyle("-fx-text-fill: grey; -fx-padding: 3;");
                }

                miniCalendarGrid.add(dayLabel, col, row);
                dateIterator = dateIterator.plusDays(1);
            }
        }
    }

    private void setupUserMenu(String userName) {
        userIconContainer.setCursor(Cursor.HAND);

        ContextMenu contextMenu = new ContextMenu();

        MenuItem userItem = new MenuItem("Usuario: " + userName);
        userItem.setDisable(true);
        userItem.setStyle("-fx-opacity: 1.0; -fx-font-weight: bold; -fx-text-fill: black;");

        MenuItem logoutItem = new MenuItem("Cerrar sesión");
        logoutItem.setOnAction(e -> logout());

        contextMenu.getItems().addAll(userItem, logoutItem);

        userIconContainer.setOnMouseClicked(e -> {
            contextMenu.show(userIconContainer, Side.BOTTOM, 0, 0);
        });
    }

    private void logout() {
        // Limpiar sesión
        Session.getInstance().setUsuario(null);
        Session.getInstance().setCliente(null);

        // Volver a login
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 800);
            Stage stage = (Stage) userIconContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

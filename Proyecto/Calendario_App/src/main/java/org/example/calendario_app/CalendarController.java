package org.example.calendario_app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import javafx.geometry.Side;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.Priority;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.example.calendario_app.util.Session;
import org.example.calendario_app.model.Evento;
import org.example.calendario_app.model.Etiqueta;
import org.example.calendario_app.dao.EventoDAO;
import org.example.calendario_app.dao.EtiquetaDAO;
import org.example.calendario_app.dao.impl.EventoDAOImpl;
import org.example.calendario_app.dao.impl.EtiquetaDAOImpl;
import org.example.calendario_app.dao.FestivoDAO;
import org.example.calendario_app.dao.impl.FestivoDAOImpl;
import org.example.calendario_app.model.Festivo;

import org.example.calendario_app.model.Grupo;
import org.example.calendario_app.dao.GrupoDAO;
import org.example.calendario_app.dao.impl.GrupoDAOImpl;
import org.example.calendario_app.dao.UsuarioDAO;
import org.example.calendario_app.dao.impl.UsuarioDAOImpl;
// import org.example.calendario_app.model.Usuario; // Unused
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
    private Button btnToday;

    @FXML
    private Label miniMonthLabel;

    @FXML
    private Label userInitialLabel;

    @FXML
    private StackPane userIconContainer;

    @FXML
    private GridPane miniCalendarGrid;

    @FXML
    private ToggleButton btnViewDay;

    @FXML
    private ToggleButton btnViewWeek;

    @FXML
    private ToggleButton btnViewMonth;

    private enum CalendarView {
        MONTH, WEEK, DAY
    }

    private CalendarView currentView = CalendarView.MONTH;
    private LocalDate currentDate;

    @FXML
    private Button btnCreateEvent;

    @FXML
    private Button btnCreateLabel;

    @FXML
    private Button btnCreateGroup;

    @FXML
    private VBox myCalendarsContainer;

    @FXML
    private VBox sharedCalendarsContainer;

    // private YearMonth currentYearMonth; // Replaced by currentDate
    private final List<Evento> events = new ArrayList<>();
    private final List<Etiqueta> labels = new ArrayList<>();
    private EventoDAO eventoDAO;
    private EtiquetaDAO etiquetaDAO;
    private GrupoDAO grupoDAO;
    private UsuarioDAO usuarioDAO;
    private FestivoDAO festivoDAO;
    private final List<Festivo> holidays = new ArrayList<>();

    @FXML
    public void initialize() {
        eventoDAO = new EventoDAOImpl();
        etiquetaDAO = new EtiquetaDAOImpl();
        grupoDAO = new GrupoDAO(new GrupoDAOImpl());
        usuarioDAO = new UsuarioDAO(new UsuarioDAOImpl());
        festivoDAO = new FestivoDAOImpl();

        loadEvents();
        loadUserGroups(); // New method
        loadLabels();
        loadHolidays();

        currentDate = LocalDate.now();

        if (Session.getInstance().getUsuario() != null) {
            if (Session.getInstance().getCliente() != null) {
                String initName = Session.getInstance().getCliente().getNombre();
                if (initName != null && !initName.isEmpty()) {
                    userInitialLabel.setText(initName.substring(0, 1).toUpperCase());
                    setupUserMenu(initName);
                }
            }
        }

        drawCalendar();
        drawMiniCalendar();

        btnPrev.setOnAction(e -> {
            switch (currentView) {
                case MONTH -> currentDate = currentDate.minusMonths(1);
                case WEEK -> currentDate = currentDate.minusWeeks(1);
                case DAY -> currentDate = currentDate.minusDays(1);
            }
            drawCalendar();
            drawMiniCalendar();
        });

        btnNext.setOnAction(e -> {
            switch (currentView) {
                case MONTH -> currentDate = currentDate.plusMonths(1);
                case WEEK -> currentDate = currentDate.plusWeeks(1);
                case DAY -> currentDate = currentDate.plusDays(1);
            }
            drawCalendar();
            drawMiniCalendar();
        });

        btnCreateEvent.setOnAction(e -> openCreateEventDialog());
        btnCreateLabel.setOnAction(e -> openCreateLabelDialog());
        btnCreateGroup.setOnAction(e -> openCreateGroupDialog());
    }

    private final java.util.Map<Integer, List<Integer>> groupToMemberIds = new java.util.HashMap<>();

    private void loadUserGroups() {
        if (Session.getInstance().getUsuario() != null) {
            int userId = Session.getInstance().getUsuario().getId();
            List<Grupo> userGroups = grupoDAO.findAllByUserId(userId);
            sharedCalendarsContainer.getChildren().clear(); // Clear existing
            groupToMemberIds.clear();

            for (Grupo group : userGroups) {
                // Load members for this group
                List<Integer> memberIds = grupoDAO.findMembersByGroupId(group.getId_grupo());
                groupToMemberIds.put(group.getId_grupo(), memberIds);

                addGroupCheckBox(group);
            }
        }
    }

    private void addGroupCheckBox(Grupo group) {
        CheckBox newGroup = new CheckBox(group.getNombre());
        newGroup.setSelected(true);
        newGroup.getStyleClass().add("calendar-check-blue"); // Ensure this class exists or default color logic
        newGroup.setUserData(group.getId_grupo());

        newGroup.setOnAction(e -> drawCalendar());

        sharedCalendarsContainer.getChildren().add(newGroup);
    }

    private void openCreateGroupDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("group-dialog.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Crear Nuevo Grupo");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnCreateGroup.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            GroupDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setAvailableLabels(labels); // Pass existing labels

            // Pass DAOs for label creation within group dialog
            controller.setEtiquetaDAO(etiquetaDAO);
            controller.setGrupoDAO(grupoDAO);
            controller.setUsuarioDAO(usuarioDAO);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                String groupName = controller.getGroupName();
                // Get description if available from controller, or default
                // For now assuming we just have name in this block, but controller has
                // descriptionArea
                // casting controller to access fields if getters missing or update controller
                // Actually GroupDialogController needs getGroupDescription()

                if (groupName != null && !groupName.isEmpty()) {
                    if (Session.getInstance().getUsuario() != null) {
                        String description = controller.getGroupDescription();
                        if (description == null)
                            description = "";
                        Grupo newGroup = new Grupo(groupName, description);
                        int creatorId = Session.getInstance().getUsuario().getId();
                        int groupId = grupoDAO.create(newGroup, creatorId);

                        if (groupId != -1) {
                            newGroup.setId_grupo(groupId);

                            // Add invited members
                            List<GroupDialogController.GroupMember> members = controller.getMembers();
                            System.out.println("DEBUG: Members to add: " + members.size());

                            // Get current user name for the email
                            String currentUserName = "Un usuario";
                            if (Session.getInstance().getCliente() != null) {
                                currentUserName = Session.getInstance().getCliente().getNombre();
                            }

                            for (GroupDialogController.GroupMember member : members) {
                                if (member.usuario != null) {
                                    System.out.println("DEBUG: Adding member ID: " + member.usuario.getId());

                                    String role = member.role;
                                    // Normalize role for database (Usuario -> miembro, Admin -> admin)
                                    if ("Usuario".equalsIgnoreCase(role)) {
                                        role = "miembro";
                                    } else if ("Admin".equalsIgnoreCase(role)) {
                                        role = "admin";
                                    }

                                    System.out.println(
                                            "DEBUG: Role from Object: " + member.role + " -> Converted: " + role);

                                    boolean added = false;
                                    if (!grupoDAO.addMember(groupId, member.usuario.getId(), role)) {
                                        System.out.println("DEBUG: Failed to add member with role '" + role
                                                + "'. Trying 'miembro'...");
                                        // Retry with "miembro" as fallback
                                        if (!grupoDAO.addMember(groupId, member.usuario.getId(), "miembro")) {
                                            System.out.println("DEBUG: Failed with 'miembro'. Trying 'user'...");
                                            // Last resort retry with "user" just in case
                                            if (grupoDAO.addMember(groupId, member.usuario.getId(), "user")) {
                                                added = true;
                                            } else {
                                                System.err.println("ERROR: Failed to add member after all retries.");
                                            }
                                        } else {
                                            added = true;
                                        }
                                    } else {
                                        added = true;
                                    }

                                    if (added) {
                                        // Send Email Invitation
                                        String recipientEmail = member.usuario.getCorreo();
                                        // Use the exact role string shown in UI or the database one? Let's use the UI
                                        // one for friendliness or the converted one.
                                        // The template uses $role.
                                        Mail.sendGroupInvitation(recipientEmail, "Usuario", currentUserName, groupName,
                                                member.role);
                                    }
                                }
                            }

                            // Link selected labels to the new group (New)
                            List<Etiqueta> selectedLabels = controller.getSelectedLabels();
                            for (Etiqueta label : selectedLabels) {
                                etiquetaDAO.updateGroupId(label.getId(), groupId);
                            }

                            // Refresh labels to update their group ID in memory
                            loadLabels();

                            addGroupCheckBox(newGroup);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openCreateLabelDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("label-dialog.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Crear Etiqueta");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnCreateLabel.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            LabelDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Pass available groups
            if (Session.getInstance().getUsuario() != null) {
                int userId = Session.getInstance().getUsuario().getId();
                List<Grupo> userGroups = grupoDAO.findAllByUserId(userId);
                controller.setAvailableGroups(userGroups);
            }

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                String labelName = controller.getLabelName();
                String labelColorClass = controller.getLabelColorClass();
                Grupo selectedGroup = controller.getSelectedGroup();

                if (labelName != null && !labelName.isEmpty() && labelColorClass != null) {
                    Etiqueta newLabel = new Etiqueta(labelName, labelColorClass);

                    if (Session.getInstance().getUsuario() != null) {
                        int userId = Session.getInstance().getUsuario().getId();
                        Integer groupId = (selectedGroup != null) ? selectedGroup.getId_grupo() : null;

                        etiquetaDAO.save(newLabel, userId, groupId);
                        // If linking to group, maybe UI update is different?
                        // For now just reload labels or add to sidebar if sidebar shows personal
                        // labels.
                        // Currently 'loadLabels()' adds to sidebar.
                        // If it's a group label, should it appear in sidebar?
                        // Assuming yes for now, re-load or add.
                        loadLabels();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addCalendarLabelUI(Etiqueta etiqueta) {
        CheckBox newCalendar = new CheckBox(etiqueta.getNombre());
        newCalendar.setSelected(true);
        // Handle both short codes ("purple") and legacy full classes
        // ("calendar-check-purple")
        String color = etiqueta.getColor();
        if (color != null && !color.startsWith("calendar-check-")) {
            color = "calendar-check-" + color;
        }
        newCalendar.getStyleClass().add(color);

        // Store Label ID in user data for filtering
        newCalendar.setUserData(etiqueta.getId());

        // Redraw calendar when checkbox toggled
        newCalendar.setOnAction(e -> drawCalendar());

        myCalendarsContainer.getChildren().add(newCalendar);
    }

    private void loadEvents() {
        if (Session.getInstance().getUsuario() != null) {
            events.clear();
            events.addAll(eventoDAO.findAllByUsuarioId(Session.getInstance().getUsuario().getId()));
        }
    }

    private void loadLabels() {
        myCalendarsContainer.getChildren().clear();
        labels.clear();
        if (Session.getInstance().getUsuario() != null) {
            labels.addAll(etiquetaDAO.findAllByUsuarioId(Session.getInstance().getUsuario().getId()));
            for (Etiqueta etiqueta : labels) {
                addCalendarLabelUI(etiqueta);
            }
        }
    }

    private void loadHolidays() {
        holidays.clear();
        holidays.addAll(festivoDAO.findAll());
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
            controller.setLabels(labels);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                Evento newEvento = controller.getEvento();
                if (Session.getInstance().getUsuario() != null) {
                    System.out.println(
                            "DEBUG: Current User ID in Session = " + Session.getInstance().getUsuario().getId());
                    newEvento.setId_creador(Session.getInstance().getUsuario().getId());
                    if (eventoDAO.save(newEvento) > 0) {
                        events.add(newEvento);
                        drawCalendar(); // Refresh to show new event
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToToday() {
        currentDate = LocalDate.now();
        drawCalendar();
        drawMiniCalendar();
    }

    @FXML
    public void onViewChanged() {
        if (btnViewDay.isSelected()) {
            currentView = CalendarView.DAY;
        } else if (btnViewWeek.isSelected()) {
            currentView = CalendarView.WEEK;
        } else {
            currentView = CalendarView.MONTH;
        }
        drawCalendar();
    }

    private void drawCalendar() {
        // Clear grid entirely (headers will be re-added or kept depending on view)
        // Simplest strategy: Clear all children and redraw headers + content per view
        calendarGrid.getChildren().clear();

        // Update Header Label based on view
        DateTimeFormatter formatter;
        String formattedDate;

        switch (currentView) {
            case MONTH -> {
                formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-ES"));
                formattedDate = currentDate.format(formatter);
                monthYearLabel.setText(formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1));
                drawMonthView();
            }
            case WEEK -> {
                DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("MMM yyyy",
                        Locale.forLanguageTag("es-ES"));
                // Show range? or just Month of start date
                formattedDate = currentDate.format(weekFormatter);
                monthYearLabel.setText(
                        "Semana del " + currentDate.format(DateTimeFormatter.ofPattern("d")) + " - " + formattedDate);
                drawWeekView();
            }
            case DAY -> {
                formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("es-ES"));
                formattedDate = currentDate.format(formatter);
                monthYearLabel.setText(formattedDate);
                drawDayView();
            }
        }
    }

    private void drawWeekHeader() {
        String[] days = { "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo" };
        for (int i = 0; i < 7; i++) {
            Label label = new Label(days[i]);
            label.getStyleClass().add("day-header");
            GridPane.setHalignment(label, javafx.geometry.HPos.CENTER);
            calendarGrid.add(label, i, 0);
        }
    }

    private void drawMonthView() {
        drawWeekHeader();

        // Update Header Label - Already done in drawCalendar switch
        // Just draw the grid

        // Calculate days
        LocalDate firstOfMonth = YearMonth.from(currentDate).atDay(1);
        // ... rest of logic ...
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
                boolean isCurrentMonth = dateIterator.getMonth().equals(currentDate.getMonth());
                if (isCurrentMonth) {
                    cell.getStyleClass().add("day-cell");
                } else {
                    cell.getStyleClass().add("day-cell-dimmed");
                }

                // Day Label
                Label dayLabel = new Label(String.valueOf(dateIterator.getDayOfMonth()));
                dayLabel.getStyleClass().add("day-label");
                if (dateIterator.equals(LocalDate.now())) {
                    dayLabel.getStyleClass().add("current-day-label");
                }
                cell.getChildren().add(dayLabel);

                // Add Holidays
                for (Festivo festivo : holidays) {
                    if (festivo.getDia() == dateIterator.getDayOfMonth()
                            && festivo.getMes() == dateIterator.getMonthValue()) {
                        addHolidayLabel(cell, festivo);
                    }
                }

                // Add User Created Events

                // 1. Collect active group IDs
                List<Integer> activeGroupIds = new ArrayList<>();
                for (javafx.scene.Node node : sharedCalendarsContainer.getChildren()) {
                    if (node instanceof CheckBox) {
                        CheckBox cb = (CheckBox) node;
                        if (cb.isSelected() && cb.getUserData() instanceof Integer) {
                            activeGroupIds.add((Integer) cb.getUserData());
                        }
                    }
                }
                System.out.println("[DEBUG] Active Group IDs: " + activeGroupIds);

                // 2. Collect visible label IDs (filtering out those from inactive groups)
                List<Integer> visibleLabelIds = new ArrayList<>();
                for (javafx.scene.Node node : myCalendarsContainer.getChildren()) {
                    if (node instanceof CheckBox) {
                        CheckBox cb = (CheckBox) node;
                        if (cb.isSelected() && cb.getUserData() instanceof Integer) {
                            int labelId = (Integer) cb.getUserData();
                            // Find label object
                            Etiqueta labelObj = null;
                            for (Etiqueta l : labels) {
                                if (l.getId() == labelId) {
                                    labelObj = l;
                                    break;
                                }
                            }

                            if (labelObj != null && labelObj.getId_grupo() != null) {
                                // Group label: only add if group is active
                                if (activeGroupIds.contains(labelObj.getId_grupo())) {
                                    visibleLabelIds.add(labelId);
                                }
                            } else {
                                visibleLabelIds.add(labelId);
                            }
                        }
                    }
                }

                // 3. Collect visible creator IDs from Shared Groups
                List<Integer> visibleCreatorIds = new ArrayList<>();
                for (Integer groupId : activeGroupIds) {
                    List<Integer> members = groupToMemberIds.get(groupId);
                    if (members != null) {
                        visibleCreatorIds.addAll(members);
                    }
                }

                for (Evento event : events) {
                    // Check if event should be visible
                    // 1. If it has a label, is that label checked? (Personal events mostly)
                    // 2. OR is the creator in the visibleCreatorIds list? (Shared events)

                    boolean isVisible = false;

                    // Condition 1: Label Check
                    if (event.getId_etiqueta() != null) {
                        if (visibleLabelIds.contains(event.getId_etiqueta())) {
                            isVisible = true;
                        }
                    } else if (visibleLabelIds.isEmpty() && visibleCreatorIds.isEmpty()) {
                        // If everything hidden, hide. If no specific label and no label filters active?
                        // Usually events with no label show up if created by user?
                        // Let's assume default events (no label) for current user are always shown
                        // unless we specifically filter them out?
                        // For now, let's stick to explicit visibility:
                        // If my Personal "No Label" was a checkbox, we'd check it.
                        // Assuming current user events are covered by "My Calendars" logic usually.
                        // But wait, "My Calendars" are LABELS. What about events with no label?
                        // If event.id_creator == current_user, we might show it?
                    }

                    // Condition 2: Shared Group Check
                    // If not yet visible, check if creator is in a shared group
                    if (!isVisible) {
                        if (visibleCreatorIds.contains(event.getId_creador())) {
                            isVisible = true;
                        }
                    }

                    // Special case for current user events with no label?
                    // Ensure they are visible if... well, we don't have a "No Label" checkbox.
                    // Let's assume if it wasn't caught by Label Filter, and it's mine, it might be
                    // visible?
                    // Or if it matches Creator ID (which is me, and I am in my groups usually).
                    // Use simpler logic:

                    // If event has label -> must match visibleLabelIds
                    // If event matches visibleCreatorIds -> visible

                    // Refined Logic:
                    boolean visibleContext = false;
                    int currentUserId = Session.getInstance().getUsuario().getId();

                    if (event.getId_creador() == currentUserId) {
                        if (event.getId_etiqueta() == null) {
                            visibleContext = true;
                        } else if (visibleLabelIds.contains(event.getId_etiqueta())) {
                            visibleContext = true;
                        }
                    } else {
                        if (visibleCreatorIds.contains(event.getId_creador())) {
                            // If the event has a label, only hide it if the label is in our sidebar AND
                            // unchecked.
                            // If the label is NOT in our sidebar, we can't filter it, so show it.
                            boolean labelVisible = true;
                            if (event.getId_etiqueta() != null) {
                                boolean userHasThisLabel = false;
                                for (Etiqueta l : labels) {
                                    if (l.getId() == event.getId_etiqueta()) {
                                        userHasThisLabel = true;
                                        break;
                                    }
                                }

                                if (userHasThisLabel && !visibleLabelIds.contains(event.getId_etiqueta())) {
                                    labelVisible = false;
                                }
                            }

                            if (labelVisible) {
                                visibleContext = true;
                            }
                        }
                    }

                    if (visibleContext && event.getFecha().equals(dateIterator)) {
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

    private void drawWeekView() {
        drawWeekHeader();

        // Find start of week (Monday)
        LocalDate startOfWeek = currentDate.minusDays(currentDate.getDayOfWeek().getValue() - 1);
        LocalDate dateIterator = startOfWeek;

        // One row of tall cells
        for (int col = 0; col < 7; col++) {
            VBox cell = createDayCell(dateIterator, false);
            cell.setPrefHeight(600); // Taller for week view
            GridPane.setVgrow(cell, javafx.scene.layout.Priority.ALWAYS);
            calendarGrid.add(cell, col, 1);
            dateIterator = dateIterator.plusDays(1);
        }
    }

    private void drawDayView() {
        calendarGrid.getChildren().clear();

        // 1. Create ScrollPane for vertical scrolling
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        // Style class for scroll pane if needed
        scrollPane.getStyleClass().add("timeline-scroll-pane");

        // 2. Create the Timeline Grid
        GridPane timelineGrid = new GridPane();
        timelineGrid.getStyleClass().add("timeline-grid");

        scrollPane.setContent(timelineGrid); // Fix: Set content

        // Constraints: Col 0 for Time (Fixed), Col 1 for Events (Grow)
        javafx.scene.layout.ColumnConstraints colTime = new javafx.scene.layout.ColumnConstraints();
        colTime.setPercentWidth(10); // ~10% width for time
        colTime.setHalignment(javafx.geometry.HPos.RIGHT);

        javafx.scene.layout.ColumnConstraints colEvents = new javafx.scene.layout.ColumnConstraints();
        colEvents.setPercentWidth(90); // ~90% for content
        colEvents.setHgrow(Priority.ALWAYS);

        timelineGrid.getColumnConstraints().addAll(colTime, colEvents);

        // 3. Populate Rows (0 to 23 hours)
        for (int hour = 0; hour < 24; hour++) {
            // Row Constraints (Fixed height per hour, e.g., 60px)
            RowConstraints rowConst = new RowConstraints();
            rowConst.setMinHeight(60);
            rowConst.setPrefHeight(60);
            rowConst.setVgrow(Priority.NEVER);
            timelineGrid.getRowConstraints().add(rowConst);

            // Time Label
            Label timeLabel = new Label(String.format("%02d:00", hour));
            timeLabel.getStyleClass().add("timeline-time-label");
            // Add margin to separate from line
            GridPane.setMargin(timeLabel, new javafx.geometry.Insets(0, 10, 0, 0));
            // Align to top-right of cell
            GridPane.setValignment(timeLabel, javafx.geometry.VPos.TOP);
            timelineGrid.add(timeLabel, 0, hour);

            // Horizontal Line / Cell Border
            // We can style the cell itself, or add a container
            VBox eventContainer = new VBox();
            eventContainer.getStyleClass().add("timeline-cell");
            // Add top border via CSS to simulate grid line
            timelineGrid.add(eventContainer, 1, hour);
        }

        // 4. Place Events
        // 1. Collect active group IDs
        List<Integer> activeGroupIds = new ArrayList<>();
        for (javafx.scene.Node node : sharedCalendarsContainer.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected() && cb.getUserData() instanceof Integer) {
                    activeGroupIds.add((Integer) cb.getUserData());
                }
            }
        }

        // 2. Collect visible label IDs (filtering out those from inactive groups)
        List<Integer> visibleLabelIds = new ArrayList<>();
        for (javafx.scene.Node node : myCalendarsContainer.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected() && cb.getUserData() instanceof Integer) {
                    int labelId = (Integer) cb.getUserData();
                    // Find label object
                    Etiqueta labelObj = null;
                    for (Etiqueta l : labels) {
                        if (l.getId() == labelId) {
                            labelObj = l;
                            break;
                        }
                    }

                    if (labelObj != null && labelObj.getId_grupo() != null) {
                        // Group label: only add if group is active
                        if (activeGroupIds.contains(labelObj.getId_grupo())) {
                            visibleLabelIds.add(labelId);
                        }
                    } else {
                        visibleLabelIds.add(labelId);
                    }
                }
            }
        }

        // 3. Collect visible creator IDs from Shared Groups
        List<Integer> visibleCreatorIds = new ArrayList<>();
        for (Integer groupId : activeGroupIds) {
            List<Integer> members = groupToMemberIds.get(groupId);
            if (members != null) {
                visibleCreatorIds.addAll(members);
            }
        }

        for (Evento event : events) {
            if (!event.getFecha().equals(currentDate))
                continue;

            boolean visibleContext = false;
            int currentUserId = Session.getInstance().getUsuario().getId();

            if (event.getId_creador() == currentUserId) {
                if (event.getId_etiqueta() == null) {
                    visibleContext = true;
                } else if (visibleLabelIds.contains(event.getId_etiqueta())) {
                    visibleContext = true;
                }
            } else {
                // Shared Event
                if (visibleCreatorIds.contains(event.getId_creador())) {
                    boolean labelVisible = true;
                    if (event.getId_etiqueta() != null) {
                        boolean userHasThisLabel = false;
                        for (Etiqueta l : labels) {
                            if (l.getId() == event.getId_etiqueta()) {
                                userHasThisLabel = true;
                                break;
                            }
                        }
                        if (userHasThisLabel && !visibleLabelIds.contains(event.getId_etiqueta())) {
                            labelVisible = false;
                        }
                    }
                    if (labelVisible) {
                        visibleContext = true;
                    }
                }
            }

            if (!visibleContext)
                continue;

            int startHour = event.getFecha_inicio().getHour();
            int endHour = event.getFecha_fin().getHour();

            // Handle edge case where end time is 00:00 of next day (shows as 0 hour?)
            // Or if start == end (less than 1 hour duration)
            if (endHour <= startHour) {
                endHour = startHour + 1; // Minimum 1 row span visually
            }
            // Cap at 24 logic if needed, but simple for now

            int rowSpan = endHour - startHour;

            // Create Event Node
            VBox eventNode = new VBox();
            eventNode.getStyleClass().add("timeline-event-entry");
            // Add color style
            String styleClass = "event-label-purple"; // Fallback
            if (event.getId_etiqueta() != null) {
                for (Etiqueta label : labels) {
                    if (label.getId() == event.getId_etiqueta()) {
                        String labelColor = label.getColor();
                        if (labelColor != null && !labelColor.isEmpty()) {
                            String shortColor = labelColor.replace("calendar-check-", "");
                            styleClass = "event-label-" + shortColor;
                        }
                        break;
                    }
                }
            }
            eventNode.getStyleClass().add(styleClass);

            Label titleLabel = new Label(event.getTitulo());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

            String times = event.getFecha_inicio().format(DateTimeFormatter.ofPattern("HH:mm")) +
                    " - " + event.getFecha_fin().format(DateTimeFormatter.ofPattern("HH:mm"));
            Label timeRangeLabel = new Label(times);
            timeRangeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 0.9em;");

            eventNode.getChildren().addAll(titleLabel, timeRangeLabel);

            // Click action
            eventNode.setOnMouseClicked(e -> {
                e.consume();
                openEventDetailsDialog(event);
            });
            eventNode.setCursor(Cursor.HAND);

            // Add to Grid
            timelineGrid.add(eventNode, 1, startHour);
            GridPane.setRowSpan(eventNode, rowSpan);
            // Allow it to fill width
            GridPane.setHgrow(eventNode, Priority.ALWAYS);
            GridPane.setFillWidth(eventNode, true);
            // Margin to avoid overlapping lines slightly
            GridPane.setMargin(eventNode, new javafx.geometry.Insets(2, 5, 2, 0));
        }

        // Add ScrollPane to main grid
        // Spanning all rows/cols of the base calendarGrid
        calendarGrid.add(scrollPane, 0, 0);
        GridPane.setColumnSpan(scrollPane, 7); // Span full width
        GridPane.setRowSpan(scrollPane, 6); // Span full height
    }

    private VBox createDayCell(LocalDate date, boolean dimNonCurrentMonth) {
        VBox cell = new VBox();
        cell.setSpacing(2);

        boolean isCurrentMonth = date.getMonth().equals(currentDate.getMonth());
        if (!dimNonCurrentMonth || isCurrentMonth) {
            cell.getStyleClass().add("day-cell");
        } else {
            cell.getStyleClass().add("day-cell-dimmed");
        }

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.getStyleClass().add("day-label");
        if (date.equals(LocalDate.now())) {
            dayLabel.getStyleClass().add("current-day-label");
        }
        cell.getChildren().add(dayLabel);

        // Add Holidays
        for (Festivo festivo : holidays) {
            if (festivo.getDia() == date.getDayOfMonth() && festivo.getMes() == date.getMonthValue()) {
                addHolidayLabel(cell, festivo);
            }
        }

        // Add Events
        // 1. Collect active group IDs
        List<Integer> activeGroupIds = new ArrayList<>();
        for (javafx.scene.Node node : sharedCalendarsContainer.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected() && cb.getUserData() instanceof Integer) {
                    activeGroupIds.add((Integer) cb.getUserData());
                }
            }
        }

        // 2. Collect visible label IDs (filtering out those from inactive groups)
        List<Integer> visibleLabelIds = new ArrayList<>();
        for (javafx.scene.Node node : myCalendarsContainer.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected() && cb.getUserData() instanceof Integer) {
                    int labelId = (Integer) cb.getUserData();
                    // Find label object
                    Etiqueta labelObj = null;
                    for (Etiqueta l : labels) {
                        if (l.getId() == labelId) {
                            labelObj = l;
                            break;
                        }
                    }

                    if (labelObj != null && labelObj.getId_grupo() != null) {
                        // Group label: only add if group is active
                        if (activeGroupIds.contains(labelObj.getId_grupo())) {
                            visibleLabelIds.add(labelId);
                        }
                    } else {
                        visibleLabelIds.add(labelId);
                    }
                }
            }
        }

        // 3. Collect visible creator IDs from Shared Groups
        List<Integer> visibleCreatorIds = new ArrayList<>();
        for (Integer groupId : activeGroupIds) {
            List<Integer> members = groupToMemberIds.get(groupId);
            if (members != null) {
                visibleCreatorIds.addAll(members);
            }
        }

        for (Evento event : events) {
            boolean visibleContext = false;
            int currentUserId = Session.getInstance().getUsuario().getId();

            if (event.getId_creador() == currentUserId) {
                if (event.getId_etiqueta() == null) {
                    visibleContext = true;
                } else if (visibleLabelIds.contains(event.getId_etiqueta())) {
                    visibleContext = true;
                }
            } else {
                if (visibleCreatorIds.contains(event.getId_creador())) {
                    boolean labelVisible = true;
                    if (event.getId_etiqueta() != null) {
                        boolean userHasThisLabel = false;
                        for (Etiqueta l : labels) {
                            if (l.getId() == event.getId_etiqueta()) {
                                userHasThisLabel = true;
                                break;
                            }
                        }
                        if (userHasThisLabel && !visibleLabelIds.contains(event.getId_etiqueta())) {
                            labelVisible = false;
                        }
                    }
                    if (labelVisible) {
                        visibleContext = true;
                    }
                }
            }

            if (visibleContext && event.getFecha().equals(date)) {
                addEventLabel(cell, event);
            }
        }
        return cell;
    }

    private void openEventDetailsDialog(Evento event) {
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
            controller.setEvento(event);

            dialogStage.showAndWait();

            if (controller.isDeleted()) {
                events.remove(event);
                drawCalendar();
                drawMiniCalendar();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addEventLabel(VBox cell, Evento event) {
        // Format time "HH:mm"
        String timeStr = event.getFecha_inicio().format(DateTimeFormatter.ofPattern("HH:mm"));
        Label eventLabel = new Label(timeStr + " " + event.getTitulo());

        // Default style
        String styleClass = "event-label-purple";

        // Find label color if exists
        if (event.getId_etiqueta() != null) {
            for (Etiqueta label : labels) {
                if (label.getId() == event.getId_etiqueta()) {
                    // Label color could be short code e.g. "red" or legacy "calendar-check-red"
                    // Map to event-label-COLOR e.g. event-label-red
                    String labelColor = label.getColor();
                    if (labelColor != null && !labelColor.isEmpty()) {
                        String shortColor = labelColor.replace("calendar-check-", "");
                        styleClass = "event-label-" + shortColor;
                    }
                    break;
                }
            }
        }

        eventLabel.getStyleClass().add(styleClass);
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

    private void addHolidayLabel(VBox cell, Festivo festivo) {
        Label paramLabel = new Label("★ " + festivo.getNombre());
        paramLabel.getStyleClass().add("event-label-holiday");
        paramLabel.setWrapText(true);
        cell.getChildren().add(paramLabel);
    }

    private void drawMiniCalendar() {
        // Update Header Label
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-ES"));
        String formattedDate = currentDate.format(formatter);
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        miniMonthLabel.setText(formattedDate);

        // Clear grid but keep headers
        miniCalendarGrid.getChildren().removeIf(node -> {
            Integer rowIndex = GridPane.getRowIndex(node);
            return rowIndex != null && rowIndex > 0;
        });

        // Calculate days (same as main calendar)
        LocalDate firstOfMonth = YearMonth.from(currentDate).atDay(1);
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue();
        int offset = dayOfWeekValue - 1;
        LocalDate dateIterator = firstOfMonth.minusDays(offset);

        // Draw rows
        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                Label dayLabel = new Label(String.valueOf(dateIterator.getDayOfMonth()));
                boolean isCurrentMonth = dateIterator.getMonth().equals(currentDate.getMonth());

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
        contextMenu.getStyleClass().add("custom-context-menu");

        MenuItem userItem = new MenuItem("Usuario: " + userName);
        userItem.setDisable(true);

        MenuItem changePasswordItem = new MenuItem("Cambiar contraseña");
        changePasswordItem.setOnAction(e -> openChangePasswordDialog());

        MenuItem logoutItem = new MenuItem("Cerrar sesión");
        logoutItem.setOnAction(e -> logout());

        contextMenu.getItems().addAll(userItem, changePasswordItem, logoutItem);

        userIconContainer.setOnMouseClicked(e -> {
            contextMenu.show(userIconContainer, Side.BOTTOM, 0, 0);
        });
    }

    private void logout() {
        // Limpiar sesión
        Session.getInstance().setUsuario(null);
        Session.getInstance().setCliente(null);

        // Limpiar credenciales guardadas
        org.example.calendario_app.util.PrefsManager.clear();

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

    private void openChangePasswordDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("change-password-dialog.fxml"));
            VBox page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Cambiar Contraseña");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(userIconContainer.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            ChangePasswordDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                String newPassword = controller.getNewPassword();
                if (newPassword != null && !newPassword.isEmpty()) {
                    if (Session.getInstance().getUsuario() != null) {
                        int userId = Session.getInstance().getUsuario().getId();
                        boolean success = usuarioDAO.updatePassword(userId, newPassword);

                        if (success) {
                            // Send Email Notification
                            String email = Session.getInstance().getUsuario().getCorreo();
                            String name = "Usuario";
                            if (Session.getInstance().getCliente() != null) {
                                name = Session.getInstance().getCliente().getNombre();
                            }
                            Mail.sendPasswordChangeNotification(email, name);
                        }

                        Alert alert;
                        if (success) {
                            alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Éxito");
                            alert.setHeaderText(null);
                            alert.setContentText("Contraseña actualizada correctamente.");

                            // Update saved credentials if auto-login is used
                            if (org.example.calendario_app.util.PrefsManager.getEmail() != null) {
                                org.example.calendario_app.util.PrefsManager.saveCreds(
                                        Session.getInstance().getUsuario().getCorreo(), newPassword);
                            }
                        } else {
                            alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("No se pudo actualizar la contraseña en la base de datos.");
                        }
                        alert.showAndWait();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

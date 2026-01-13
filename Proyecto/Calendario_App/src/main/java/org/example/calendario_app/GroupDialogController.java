package org.example.calendario_app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.calendario_app.model.Etiqueta;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import org.example.calendario_app.dao.EtiquetaDAO;
import org.example.calendario_app.dao.GrupoDAO;
import org.example.calendario_app.dao.impl.EtiquetaDAOImpl;
import org.example.calendario_app.model.Grupo;
import org.example.calendario_app.util.Session;
import java.io.IOException;

public class GroupDialogController {

    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<Etiqueta> labelComboBox;
    @FXML
    private FlowPane selectedLabelsContainer;

    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private VBox membersContainer;

    private Stage dialogStage;
    private boolean saveClicked = false;
    private List<Etiqueta> availableLabels;
    private final List<Etiqueta> selectedLabels = new ArrayList<>();

    // Dependencies needed for creating a new label
    private EtiquetaDAO etiquetaDAO;
    private GrupoDAO grupoDAO;

    // Simple inner class or structure for Member
    public static class GroupMember {
        String email;
        String role;

        public GroupMember(String email, String role) {
            this.email = email;
            this.role = role;
        }
    }

    private final List<GroupMember> members = new ArrayList<>();

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Admin", "Usuario");
        roleComboBox.getSelectionModel().select("Usuario");

        labelComboBox.setCellFactory(lv -> new ListCell<Etiqueta>() {
            @Override
            protected void updateItem(Etiqueta item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getNombre());
            }
        });
        labelComboBox.setButtonCell(new ListCell<Etiqueta>() {
            @Override
            protected void updateItem(Etiqueta item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getNombre());
            }
        });

        labelComboBox.setOnAction(e -> {
            Etiqueta selected = labelComboBox.getSelectionModel().getSelectedItem();
            if (selected != null && !selectedLabels.contains(selected)) {
                addLabelChip(selected);
                selectedLabels.add(selected);
                labelComboBox.getSelectionModel().clearSelection();
            }
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setAvailableLabels(List<Etiqueta> labels) {
        this.availableLabels = labels;
        labelComboBox.getItems().setAll(labels);
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public String getGroupName() {
        return nameField.getText();
    }

    public String getGroupDescription() {
        return descriptionArea.getText();
    }

    public List<GroupMember> getMembers() {
        return members;
    }

    public void setEtiquetaDAO(EtiquetaDAO etiquetaDAO) {
        this.etiquetaDAO = etiquetaDAO;
    }

    public void setGrupoDAO(GrupoDAO grupoDAO) {
        this.grupoDAO = grupoDAO;
    }

    // Additional getters if needed for description, members, labels etc.

    @FXML
    private void handleAddMember() {
        String email = emailField.getText();
        String role = roleComboBox.getValue();
        if (email != null && !email.isEmpty() && role != null) {
            members.add(new GroupMember(email, role));

            // Add UI row
            HBox row = new HBox(10);
            row.setStyle("-fx-alignment: center-left; -fx-padding: 5;");
            Label emailLbl = new Label(email);
            emailLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            Label roleLbl = new Label(role);
            roleLbl.setStyle("-fx-text-fill: grey; -fx-font-size: 11px;");

            VBox info = new VBox(emailLbl, roleLbl);
            HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

            Button removeBtn = new Button("X");
            removeBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #E74C3C; -fx-cursor: hand; -fx-font-weight: bold;");
            removeBtn.setOnAction(e -> {
                members.removeIf(m -> m.email.equals(email));
                membersContainer.getChildren().remove(row);
            });

            row.getChildren().addAll(info, removeBtn);
            membersContainer.getChildren().add(row);

            emailField.clear();
        }
    }

    private void addLabelChip(Etiqueta etiqueta) {
        HBox chip = new HBox(5);
        chip.setStyle(
                "-fx-background-color: #383B42; -fx-background-radius: 15; -fx-padding: 3 10; -fx-alignment: center; -fx-border-color: #555; -fx-border-radius: 15;");

        // Add color dot
        Label dot = new Label("●");
        // Simple mapping, normally we'd parse the color class
        String colorStyle = "-fx-text-fill: white;";
        if (etiqueta.getColor() != null) {
            if (etiqueta.getColor().contains("red"))
                colorStyle = "-fx-text-fill: #E74C3C;";
            else if (etiqueta.getColor().contains("blue"))
                colorStyle = "-fx-text-fill: #3498DB;";
            else if (etiqueta.getColor().contains("green"))
                colorStyle = "-fx-text-fill: #2ECC71;";
            else if (etiqueta.getColor().contains("orange"))
                colorStyle = "-fx-text-fill: #E67E22;";
            else if (etiqueta.getColor().contains("purple"))
                colorStyle = "-fx-text-fill: #9B59B6;";
        }
        dot.setStyle(colorStyle + " -fx-font-size: 10px;");

        Label nameLbl = new Label(etiqueta.getNombre());
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        Button close = new Button("x");
        close.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #B0B0B0; -fx-padding: 0; -fx-font-size: 10px; -fx-cursor: hand;");
        close.setOnAction(e -> {
            selectedLabels.remove(etiqueta);
            selectedLabelsContainer.getChildren().remove(chip);
        });

        chip.getChildren().addAll(dot, nameLbl, close);
        selectedLabelsContainer.getChildren().add(chip);
    }

    @FXML
    private void handleCreateLengthLabel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("label-dialog.fxml"));
            VBox page = loader.load();

            Stage labelStage = new Stage();
            labelStage.setTitle("Crear Etiqueta");
            labelStage.initModality(Modality.WINDOW_MODAL);
            labelStage.initOwner(dialogStage); // Owner is this dialog
            Scene scene = new Scene(page);
            labelStage.setScene(scene);

            LabelDialogController controller = loader.getController();
            controller.setDialogStage(labelStage);

            // Pass user groups if user is logged in
            if (Session.getInstance().getUsuario() != null) {
                int userId = Session.getInstance().getUsuario().getId();
                // We need grupoDAO here
                if (grupoDAO != null) {
                    List<Grupo> userGroups = grupoDAO.findAllByUserId(userId);
                    controller.setAvailableGroups(userGroups);
                }
            }

            labelStage.showAndWait();

            if (controller.isSaveClicked()) {
                String labelName = controller.getLabelName();
                String labelColorClass = controller.getLabelColorClass();
                Grupo selectedGroup = controller.getSelectedGroup();

                if (labelName != null && !labelName.isEmpty() && labelColorClass != null && etiquetaDAO != null) {
                    Etiqueta newLabel = new Etiqueta(labelName, labelColorClass);
                    if (Session.getInstance().getUsuario() != null) {
                        int userId = Session.getInstance().getUsuario().getId();
                        Integer groupId = (selectedGroup != null) ? selectedGroup.getId_grupo() : null;
                        if (etiquetaDAO.save(newLabel, userId, groupId) > 0) {
                            // Refresh labels list in THIS dialog
                            if (availableLabels == null)
                                availableLabels = new ArrayList<>();
                            availableLabels.add(newLabel); // Add to local list
                            labelComboBox.getItems().add(newLabel); // Add to combo

                            // Automatically select and add chip?
                            addLabelChip(newLabel);
                            selectedLabels.add(newLabel);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateGroup() {
        if (nameField.getText() == null || nameField.getText().isEmpty()) {
            // Show error
            nameField.setStyle("-fx-border-color: red;");
            return;
        }

        // UX Improvement: Check if there is a pending email in the field that wasn't
        // added
        String pendingEmail = emailField.getText();
        if (pendingEmail != null && !pendingEmail.isEmpty()) {
            // Validate role
            String role = roleComboBox.getValue();
            if (role != null) {
                // Determine if this email is already in the list
                boolean alreadyAdded = members.stream().anyMatch(m -> m.email.equals(pendingEmail));
                if (!alreadyAdded) {
                    // Auto-add the member
                    members.add(new GroupMember(pendingEmail, role));
                    System.out.println("DEBUG: Auto-added pending member: " + pendingEmail);
                }
            }
        }

        System.out.println("DEBUG: Save clicked. Total members: " + members.size());
        saveClicked = true;
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}

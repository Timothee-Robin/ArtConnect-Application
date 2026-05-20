package com.project.artconnect.ui;

import com.project.artconnect.model.Workshop;
import com.project.artconnect.model.Artist;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;
import com.project.artconnect.service.ArtistService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class WorkshopController {
    @FXML private TableView<Workshop> workshopTable;
    @FXML private TableColumn<Workshop, String> titleColumn;
    @FXML private TableColumn<Workshop, LocalDateTime> dateColumn;
    @FXML private TableColumn<Workshop, String> instructorColumn;
    @FXML private TableColumn<Workshop, Double> priceColumn;
    @FXML private TableColumn<Workshop, String> levelColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        instructorColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getInstructor() != null ? cd.getValue().getInstructor().getName() : "Unknown"));
        refreshTable();
        updateButtonVisibility();
    }

    public void updateButtonVisibility() {
        // Buttons always visible — permission check happens in click handler
    }

    private MainController getMainController() { return ServiceProvider.getMainController(); }

    public void refreshTable() {
        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
    }

    @FXML private void handleAddWorkshop() {
        MainController main = getMainController();
        if (main == null || !main.isLoggedIn()) { showAlert("Access Denied", "You must be logged in to add a workshop."); return; }
        Dialog<Workshop> d = createDialog(null);
        d.showAndWait().ifPresent(w -> { workshopService.createWorkshop(w); refreshTable(); });
    }

    @FXML private void handleEditWorkshop() {
        MainController main = getMainController();
        if (main == null || !main.isAdmin()) { showAlert("Access Denied", "Only admins can edit workshops."); return; }
        Workshop s = workshopTable.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("No Selection", "Please select a workshop to edit."); return; }
        Dialog<Workshop> d = createDialog(s);
        d.showAndWait().ifPresent(w -> { workshopService.updateWorkshop(w); refreshTable(); });
    }

    @FXML private void handleDeleteWorkshop() {
        MainController main = getMainController();
        if (main == null || !main.isAdmin()) { showAlert("Access Denied", "Only admins can delete workshops."); return; }
        Workshop s = workshopTable.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("No Selection", "Please select a workshop to delete."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Confirm Deletion"); c.setHeaderText("Delete \"" + s.getTitle() + "\"?");
        c.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) { workshopService.deleteWorkshop(s.getId()); refreshTable(); } });
    }

    private Dialog<Workshop> createDialog(Workshop existing) {
        Dialog<Workshop> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New Workshop" : "Edit Workshop");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField titleField = new TextField(); titleField.setPromptText("Title *");
        DatePicker datePicker = new DatePicker(); datePicker.setPromptText("Workshop Date (YYYY-MM-DD)");
        TextField timeField = new TextField(); timeField.setPromptText("Time (HH:MM, e.g. 14:00)");
        TextField durationField = new TextField(); durationField.setPromptText("Duration in minutes (e.g. 120)");
        TextField maxPartField = new TextField(); maxPartField.setPromptText("Max Participants (e.g. 15)");
        TextField priceField = new TextField(); priceField.setPromptText("Price (e.g. 45.00)");
        TextField locationField = new TextField(); locationField.setPromptText("Location");
        ComboBox<String> levelBox = new ComboBox<>(FXCollections.observableArrayList("Beginner", "Intermediate", "Advanced", "All Levels"));
        levelBox.setPromptText("Select Level");
        TextField descField = new TextField(); descField.setPromptText("Description");
        ComboBox<Artist> instructorBox = new ComboBox<>(FXCollections.observableArrayList(artistService.getAllArtists()));
        instructorBox.setPromptText("Select Instructor");

        if (existing != null) {
            titleField.setText(existing.getTitle()); titleField.setDisable(true);
            if (existing.getDate() != null) {
                datePicker.setValue(existing.getDate().toLocalDate());
                timeField.setText(existing.getDate().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            durationField.setText(existing.getDurationMinutes() > 0 ? Integer.toString(existing.getDurationMinutes()) : "");
            maxPartField.setText(existing.getMaxParticipants() > 0 ? Integer.toString(existing.getMaxParticipants()) : "");
            priceField.setText(Double.toString(existing.getPrice()));
            locationField.setText(existing.getLocation());
            levelBox.setValue(existing.getLevel());
            descField.setText(existing.getDescription());
            instructorBox.setValue(existing.getInstructor());
        }

        grid.add(new Label("Title *:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Date:"), 0, 1); grid.add(datePicker, 1, 1);
        grid.add(new Label("Time:"), 0, 2); grid.add(timeField, 1, 2);
        grid.add(new Label("Duration (min):"), 0, 3); grid.add(durationField, 1, 3);
        grid.add(new Label("Max Participants:"), 0, 4); grid.add(maxPartField, 1, 4);
        grid.add(new Label("Price:"), 0, 5); grid.add(priceField, 1, 5);
        grid.add(new Label("Location:"), 0, 6); grid.add(locationField, 1, 6);
        grid.add(new Label("Level:"), 0, 7); grid.add(levelBox, 1, 7);
        grid.add(new Label("Instructor:"), 0, 8); grid.add(instructorBox, 1, 8);
        grid.add(new Label("Description:"), 0, 9); grid.add(descField, 1, 9);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(db -> {
            if (db == saveBtn) {
                String title = titleField.getText().trim();
                LocalDateTime date = null;
                if (datePicker.getValue() != null && !timeField.getText().trim().isEmpty()) {
                    try {
                        String timeStr = timeField.getText().trim().toLowerCase();
                        timeStr = timeStr.replace("h", ":").replace(".", ":").replace(" ", "");
                        if (!timeStr.contains(":")) {
                            timeStr += ":00";
                        }
                        String[] parts = timeStr.split(":");
                        if (parts.length >= 2) {
                            String hour = parts[0];
                            if (hour.isEmpty()) hour = "00";
                            else if (hour.length() == 1) hour = "0" + hour;
                            
                            String minute = parts[1];
                            if (minute.isEmpty()) minute = "00";
                            else if (minute.length() == 1) minute = "0" + minute;
                            
                            timeStr = hour + ":" + minute;
                        }
                        date = LocalDateTime.parse(datePicker.getValue().toString() + " " + timeStr,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    } catch (Exception e) {
                        showAlert("Validation Error", "Invalid time format. Please use HH:MM (e.g., 14:00)."); return null;
                    }
                }
                String durStr = durationField.getText().trim();
                String maxStr = maxPartField.getText().trim();
                String priceStr = priceField.getText().trim();

                if (title.isEmpty()) { showAlert("Validation Error", "Title is required."); return null; }
                if (date == null) { showAlert("Validation Error", "Date and time are required."); return null; }
                if (instructorBox.getValue() == null) { showAlert("Validation Error", "Please select an instructor."); return null; }
                if (!durStr.isEmpty()) {
                    try { int d = Integer.parseInt(durStr); if (d <= 0) { showAlert("Validation Error", "Duration must be positive."); return null; } }
                    catch (NumberFormatException e) { showAlert("Validation Error", "Duration must be a valid number."); return null; }
                }
                if (!maxStr.isEmpty()) {
                    try { int m = Integer.parseInt(maxStr); if (m <= 0) { showAlert("Validation Error", "Max participants must be positive."); return null; } }
                    catch (NumberFormatException e) { showAlert("Validation Error", "Max participants must be a valid number."); return null; }
                }
                if (!priceStr.isEmpty()) {
                    try { double p = Double.parseDouble(priceStr); if (p < 0) { showAlert("Validation Error", "Price cannot be negative."); return null; } }
                    catch (NumberFormatException e) { showAlert("Validation Error", "Price must be a valid number."); return null; }
                }

                Workshop w = new Workshop(title, date, instructorBox.getValue(),
                        priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr));
                if (!durStr.isEmpty()) w.setDurationMinutes(Integer.parseInt(durStr));
                if (!maxStr.isEmpty()) w.setMaxParticipants(Integer.parseInt(maxStr));
                w.setLocation(locationField.getText().trim());
                w.setLevel(levelBox.getValue());
                w.setDescription(descField.getText().trim());
                return w;
            }
            return null;
        });
        return dialog;
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}

package com.project.artconnect.ui;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExhibitionController {
    @FXML private TableView<Exhibition> exhibitionTable;
    @FXML private TableColumn<Exhibition, String> titleColumn;
    @FXML private TableColumn<Exhibition, LocalDate> dateColumn;
    @FXML private TableColumn<Exhibition, String> themeColumn;
    @FXML private TableColumn<Exhibition, String> galleryColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        themeColumn.setCellValueFactory(new PropertyValueFactory<>("theme"));
        galleryColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getGallery() != null ? cd.getValue().getGallery().getName() : "Unknown"));
        refreshTable();
        updateButtonVisibility();
    }

    public void updateButtonVisibility() {
        // Buttons always visible — permission check happens in click handler
    }

    private MainController getMainController() { return ServiceProvider.getMainController(); }

    public void refreshTable() {
        exhibitionTable.setItems(FXCollections.observableArrayList(galleryService.getAllExhibitions()));
    }

    @FXML private void handleAddExhibition() {
        MainController main = getMainController();
        if (main == null || !main.isLoggedIn()) { showAlert("Access Denied", "You must be logged in to add an exhibition."); return; }
        Dialog<Exhibition> d = createDialog(null);
        d.showAndWait().ifPresent(e -> { galleryService.addExhibitionToGallery(e); refreshTable(); });
    }

    @FXML private void handleEditExhibition() {
        MainController main = getMainController();
        if (main == null || !main.isAdmin()) { showAlert("Access Denied", "Only admins can edit exhibitions."); return; }
        Exhibition s = exhibitionTable.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("No Selection", "Please select an exhibition to edit."); return; }
        Dialog<Exhibition> d = createDialog(s);
        d.showAndWait().ifPresent(e -> { galleryService.updateExhibition(e); refreshTable(); });
    }

    @FXML private void handleDeleteExhibition() {
        MainController main = getMainController();
        if (main == null || !main.isAdmin()) { showAlert("Access Denied", "Only admins can delete exhibitions."); return; }
        Exhibition s = exhibitionTable.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("No Selection", "Please select an exhibition to delete."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Confirm Deletion"); c.setHeaderText("Delete \"" + s.getTitle() + "\"?");
        c.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) { galleryService.deleteExhibition(s.getId()); refreshTable(); } });
    }

    private Dialog<Exhibition> createDialog(Exhibition existing) {
        Dialog<Exhibition> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New Exhibition" : "Edit Exhibition");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField titleField = new TextField(); titleField.setPromptText("Title *");
        DatePicker startPicker = new DatePicker(); startPicker.setPromptText("Start Date (YYYY-MM-DD)");
        DatePicker endPicker = new DatePicker(); endPicker.setPromptText("End Date (YYYY-MM-DD)");
        TextField themeField = new TextField(); themeField.setPromptText("Theme (e.g. Contemporary Art)");
        TextField curatorField = new TextField(); curatorField.setPromptText("Curator Name");
        TextArea descArea = new TextArea(); descArea.setPromptText("Description"); descArea.setPrefRowCount(3);
        ComboBox<Gallery> galleryBox = new ComboBox<>(FXCollections.observableArrayList(galleryService.getAllGalleries()));
        galleryBox.setPromptText("Select Gallery");

        if (existing != null) {
            titleField.setText(existing.getTitle()); titleField.setDisable(true);
            startPicker.setValue(existing.getStartDate());
            endPicker.setValue(existing.getEndDate());
            themeField.setText(existing.getTheme());
            curatorField.setText(existing.getCuratorName());
            descArea.setText(existing.getDescription());
            galleryBox.setValue(existing.getGallery());
        }

        grid.add(new Label("Title *:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Start Date:"), 0, 1); grid.add(startPicker, 1, 1);
        grid.add(new Label("End Date:"), 0, 2); grid.add(endPicker, 1, 2);
        grid.add(new Label("Theme:"), 0, 3); grid.add(themeField, 1, 3);
        grid.add(new Label("Curator:"), 0, 4); grid.add(curatorField, 1, 4);
        grid.add(new Label("Gallery:"), 0, 5); grid.add(galleryBox, 1, 5);
        grid.add(new Label("Description:"), 0, 6); grid.add(descArea, 1, 6);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(db -> {
            if (db == saveBtn) {
                String title = titleField.getText().trim();
                LocalDate start = startPicker.getValue();
                LocalDate end = endPicker.getValue();

                if (title.isEmpty()) { showAlert("Validation Error", "Title is required."); return null; }
                if (start == null) { showAlert("Validation Error", "Start date is required."); return null; }
                if (end == null) { showAlert("Validation Error", "End date is required."); return null; }
                if (end.isBefore(start)) { showAlert("Validation Error", "End date cannot be before start date."); return null; }
                if (galleryBox.getValue() == null) { showAlert("Validation Error", "Please select a gallery."); return null; }

                Exhibition e = new Exhibition(title, start, end, galleryBox.getValue());
                if (existing != null) e.setId(existing.getId());
                e.setTheme(themeField.getText().trim());
                e.setCuratorName(curatorField.getText().trim());
                e.setDescription(descArea.getText().trim());
                return e;
            }
            return null;
        });
        return dialog;
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}

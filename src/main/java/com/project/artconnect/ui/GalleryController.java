package com.project.artconnect.ui;

import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class GalleryController {
    @FXML private ListView<Gallery> galleryList;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML public void initialize() {
        galleryList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Gallery g, boolean empty) {
                super.updateItem(g, empty);
                setText(empty || g == null ? null : g.getName() + " - " + g.getAddress() + " (" + g.getRating() + "/5.0)");
            }
        });
        refreshTable();
        updateButtonVisibility();
    }

    public void updateButtonVisibility() {
        // Buttons always visible — permission check happens in click handler
    }

    private MainController getMainController() { return ServiceProvider.getMainController(); }

    public void refreshTable() {
        galleryList.setItems(FXCollections.observableArrayList(galleryService.getAllGalleries()));
    }

    @FXML private void handleAddGallery() {
        MainController main = getMainController();
        if (main == null || !main.isLoggedIn()) { showAlert("Access Denied", "You must be logged in to add a gallery."); return; }
        Dialog<Gallery> d = createDialog(null);
        d.showAndWait().ifPresent(g -> { galleryService.createGallery(g); refreshTable(); });
    }

    @FXML private void handleEditGallery() {
        MainController main = getMainController();
        if (main == null || !main.isAdmin()) { showAlert("Access Denied", "Only admins can edit galleries."); return; }
        Gallery s = galleryList.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("No Selection", "Please select a gallery to edit."); return; }
        Dialog<Gallery> d = createDialog(s);
        d.showAndWait().ifPresent(g -> { galleryService.updateGallery(g); refreshTable(); });
    }

    @FXML private void handleDeleteGallery() {
        MainController main = getMainController();
        if (main == null || !main.isAdmin()) { showAlert("Access Denied", "Only admins can delete galleries."); return; }
        Gallery s = galleryList.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("No Selection", "Please select a gallery to delete."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Confirm Deletion"); c.setHeaderText("Delete \"" + s.getName() + "\"?");
        c.setContentText("This will also delete associated exhibitions.");
        c.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) { galleryService.deleteGallery(s.getId()); refreshTable(); } });
    }

    private Dialog<Gallery> createDialog(Gallery existing) {
        Dialog<Gallery> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New Gallery" : "Edit Gallery");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField(); nameField.setPromptText("Gallery Name *");
        TextField addressField = new TextField(); addressField.setPromptText("Address *");
        TextField ownerField = new TextField(); ownerField.setPromptText("Owner Name");
        TextField hoursField = new TextField(); hoursField.setPromptText("Opening Hours (e.g. Mon-Sat 10:00-18:00)");
        TextField phoneField = new TextField(); phoneField.setPromptText("Phone (digits only)");
        TextField ratingField = new TextField(); ratingField.setPromptText("Rating (0.0 - 5.0)");
        TextField websiteField = new TextField(); websiteField.setPromptText("Website");

        if (existing != null) {
            nameField.setText(existing.getName()); nameField.setDisable(true);
            addressField.setText(existing.getAddress());
            ownerField.setText(existing.getOwnerName());
            hoursField.setText(existing.getOpeningHours());
            phoneField.setText(existing.getContactPhone());
            ratingField.setText(Double.toString(existing.getRating()));
            websiteField.setText(existing.getWebsite());
        }

        grid.add(new Label("Name *:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1); grid.add(addressField, 1, 1);
        grid.add(new Label("Owner:"), 0, 2); grid.add(ownerField, 1, 2);
        grid.add(new Label("Hours:"), 0, 3); grid.add(hoursField, 1, 3);
        grid.add(new Label("Phone:"), 0, 4); grid.add(phoneField, 1, 4);
        grid.add(new Label("Rating:"), 0, 5); grid.add(ratingField, 1, 5);
        grid.add(new Label("Website:"), 0, 6); grid.add(websiteField, 1, 6);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(db -> {
            if (db == saveBtn) {
                String name = nameField.getText().trim();
                String address = addressField.getText().trim();
                String phone = phoneField.getText().trim();
                String ratingStr = ratingField.getText().trim();

                if (name.isEmpty()) { showAlert("Validation Error", "Gallery name is required."); return null; }
                if (address.isEmpty()) { showAlert("Validation Error", "Address is required."); return null; }
                if (!phone.isEmpty() && !phone.matches("\\d+")) { showAlert("Validation Error", "Phone must contain only digits."); return null; }
                if (!ratingStr.isEmpty()) {
                    try {
                        double r = Double.parseDouble(ratingStr);
                        if (r < 0 || r > 5) { showAlert("Validation Error", "Rating must be between 0.0 and 5.0."); return null; }
                    } catch (NumberFormatException e) { showAlert("Validation Error", "Rating must be a valid number."); return null; }
                }

                Gallery g = new Gallery(name, address, ratingStr.isEmpty() ? 0.0 : Double.parseDouble(ratingStr));
                if (existing != null) g.setId(existing.getId());
                g.setOwnerName(ownerField.getText().trim());
                g.setOpeningHours(hoursField.getText().trim());
                g.setContactPhone(phone);
                g.setWebsite(websiteField.getText().trim());
                return g;
            }
            return null;
        });
        return dialog;
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}

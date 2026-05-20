package com.project.artconnect.ui;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Artist;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.util.ServiceProvider;
import com.project.artconnect.service.ArtistService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ArtworkController {
    @FXML private TableView<Artwork> artworkTable;
    @FXML private TableColumn<Artwork, String> titleColumn;
    @FXML private TableColumn<Artwork, String> typeColumn;
    @FXML private TableColumn<Artwork, Double> priceColumn;
    @FXML private TableColumn<Artwork, String> statusColumn;
    @FXML private TableColumn<Artwork, String> artistColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        artistColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getArtist() != null ? cd.getValue().getArtist().getName() : "Unknown"));
        refreshTable();
        updateButtonVisibility();
    }

    public void updateButtonVisibility() {
        // Buttons always visible — permission check happens in click handler
    }

    private MainController getMainController() { return ServiceProvider.getMainController(); }

    public void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
    }

    @FXML private void handleAddArtwork() {
        MainController main = getMainController();
        if (main == null || !main.isLoggedIn()) { showAlert("Access Denied", "You must be logged in to add an artwork."); return; }
        Dialog<Artwork> d = createDialog(null);
        d.showAndWait().ifPresent(a -> { artworkService.createArtwork(a); refreshTable(); });
    }

    @FXML private void handleEditArtwork() {
        MainController main = getMainController();
        if (main == null || !main.isAdmin()) { showAlert("Access Denied", "Only admins can edit artworks."); return; }
        Artwork s = artworkTable.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("No Selection", "Please select an artwork to edit."); return; }
        Dialog<Artwork> d = createDialog(s);
        d.showAndWait().ifPresent(a -> { artworkService.updateArtwork(a); refreshTable(); });
    }

    @FXML private void handleDeleteArtwork() {
        MainController main = getMainController();
        if (main == null || !main.isAdmin()) { showAlert("Access Denied", "Only admins can delete artworks."); return; }
        Artwork s = artworkTable.getSelectionModel().getSelectedItem();
        if (s == null) { showAlert("No Selection", "Please select an artwork to delete."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Confirm Deletion"); c.setHeaderText("Delete \"" + s.getTitle() + "\"?");
        c.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) { artworkService.deleteArtwork(s.getId()); refreshTable(); } });
    }

    private Dialog<Artwork> createDialog(Artwork existing) {
        Dialog<Artwork> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New Artwork" : "Edit Artwork");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField titleField = new TextField(); titleField.setPromptText("Title *");
        TextField typeField = new TextField(); typeField.setPromptText("Type (e.g. painting)");
        TextField priceField = new TextField(); priceField.setPromptText("Price (e.g. 1500.00)");
        TextField yearField = new TextField(); yearField.setPromptText("Creation Year (e.g. 2024)");
        TextField mediumField = new TextField(); mediumField.setPromptText("Medium (e.g. Oil on canvas)");
        TextField dimField = new TextField(); dimField.setPromptText("Dimensions (e.g. 100x150cm)");
        ComboBox<Artist> artistBox = new ComboBox<>(FXCollections.observableArrayList(artistService.getAllArtists()));
        artistBox.setPromptText("Select Artist");

        if (existing != null) {
            titleField.setText(existing.getTitle()); titleField.setDisable(true);
            typeField.setText(existing.getType());
            priceField.setText(existing.getPrice() > 0 ? Double.toString(existing.getPrice()) : "");
            if (existing.getCreationYear() != null) yearField.setText(existing.getCreationYear().toString());
            mediumField.setText(existing.getMedium());
            dimField.setText(existing.getDimensions());
            artistBox.setValue(existing.getArtist());
        }

        grid.add(new Label("Title *:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Type:"), 0, 1); grid.add(typeField, 1, 1);
        grid.add(new Label("Price:"), 0, 2); grid.add(priceField, 1, 2);
        grid.add(new Label("Year:"), 0, 3); grid.add(yearField, 1, 3);
        grid.add(new Label("Medium:"), 0, 4); grid.add(mediumField, 1, 4);
        grid.add(new Label("Dimensions:"), 0, 5); grid.add(dimField, 1, 5);
        grid.add(new Label("Artist:"), 0, 6); grid.add(artistBox, 1, 6);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(db -> {
            if (db == saveBtn) {
                String title = titleField.getText().trim();
                String priceStr = priceField.getText().trim();
                String yearStr = yearField.getText().trim();

                if (title.isEmpty()) { showAlert("Validation Error", "Title is required."); return null; }
                if (artistBox.getValue() == null) { showAlert("Validation Error", "Please select an artist."); return null; }
                if (!priceStr.isEmpty()) {
                    try { double p = Double.parseDouble(priceStr); if (p < 0) { showAlert("Validation Error", "Price cannot be negative."); return null; } }
                    catch (NumberFormatException e) { showAlert("Validation Error", "Price must be a valid number."); return null; }
                }
                if (!yearStr.isEmpty()) {
                    try { int y = Integer.parseInt(yearStr); if (y < 1900 || y > 2030) { showAlert("Validation Error", "Year must be between 1900 and 2030."); return null; } }
                    catch (NumberFormatException e) { showAlert("Validation Error", "Year must be a valid number."); return null; }
                }
                Artwork a = new Artwork(title, yearStr.isEmpty() ? null : Integer.parseInt(yearStr), typeField.getText().trim(),
                        priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr), artistBox.getValue());
                a.setMedium(mediumField.getText().trim());
                a.setDimensions(dimField.getText().trim());
                if (existing != null) { a.setId(existing.getId()); a.setDescription(existing.getDescription()); a.setStatus(existing.getStatus()); }
                return a;
            }
            return null;
        });
        return dialog;
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}

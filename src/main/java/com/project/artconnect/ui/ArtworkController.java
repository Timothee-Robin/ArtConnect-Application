package com.project.artconnect.ui;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.project.artconnect.model.Artist;
import com.project.artconnect.service.ArtistService;

public class ArtworkController {
    @FXML
    private TableView<Artwork> artworkTable;
    @FXML
    private TableColumn<Artwork, String> titleColumn;
    @FXML
    private TableColumn<Artwork, String> typeColumn;
    @FXML
    private TableColumn<Artwork, Double> priceColumn;
    @FXML
    private TableColumn<Artwork, String> statusColumn;
    @FXML
    private TableColumn<Artwork, String> artistColumn;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        artistColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getArtist() != null ? cellData.getValue().getArtist().getName() : "Unknown"));

        refreshTable();
    }

    private void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
    }

    @FXML
    private void handleAddArtwork() {
        Dialog<Artwork> dialog = createArtworkDialog(null);
        dialog.showAndWait().ifPresent(artwork -> {
            artworkService.createArtwork(artwork);
            refreshTable();
        });
    }

    @FXML
    private void handleEditArtwork() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an artwork to edit.");
            return;
        }

        Dialog<Artwork> dialog = createArtworkDialog(selected);
        dialog.showAndWait().ifPresent(artwork -> {
            artworkService.updateArtwork(artwork);
            refreshTable();
        });
    }

    @FXML
    private void handleDeleteArtwork() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an artwork to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete " + selected.getTitle() + "?");
        confirm.setContentText("Are you sure you want to delete this artwork?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                artworkService.deleteArtwork(selected.getTitle());
                refreshTable();
            }
        });
    }

    private Dialog<Artwork> createArtworkDialog(Artwork existingArtwork) {
        Dialog<Artwork> dialog = new Dialog<>();
        dialog.setTitle(existingArtwork == null ? "Add New Artwork" : "Edit Artwork");
        dialog.setHeaderText("Enter artwork details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        TextField typeField = new TextField();
        typeField.setPromptText("Type (e.g., painting)");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        TextField yearField = new TextField();
        yearField.setPromptText("Creation Year");
        ComboBox<Artist> artistBox = new ComboBox<>(FXCollections.observableArrayList(artistService.getAllArtists()));

        if (existingArtwork != null) {
            titleField.setText(existingArtwork.getTitle());
            titleField.setDisable(true);
            typeField.setText(existingArtwork.getType());
            priceField.setText(String.valueOf(existingArtwork.getPrice()));
            yearField.setText(existingArtwork.getCreationYear() != null ? existingArtwork.getCreationYear().toString() : "");
            artistBox.setValue(existingArtwork.getArtist());
        }

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Year:"), 0, 3);
        grid.add(yearField, 1, 3);
        grid.add(new Label("Artist:"), 0, 4);
        grid.add(artistBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Integer year = null;
                try { 
                    year = !yearField.getText().isEmpty() ? Integer.parseInt(yearField.getText()) : null; 
                } catch (NumberFormatException e) {}
                
                double price = 0.0;
                try {
                    price = !priceField.getText().isEmpty() ? Double.parseDouble(priceField.getText()) : 0.0;
                } catch (NumberFormatException e) {}

                Artwork artwork = new Artwork(titleField.getText(), year, typeField.getText(), price, artistBox.getValue());
                
                if (existingArtwork != null) {
                    artwork.setMedium(existingArtwork.getMedium());
                    artwork.setDimensions(existingArtwork.getDimensions());
                    artwork.setDescription(existingArtwork.getDescription());
                    artwork.setStatus(existingArtwork.getStatus());
                }
                return artwork;
            }
            return null;
        });

        return dialog;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

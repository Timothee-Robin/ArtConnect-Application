package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ArtistController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Discipline> disciplineFilter;
    @FXML
    private TableView<Artist> artistTable;
    @FXML
    private TableColumn<Artist, String> nameColumn;
    @FXML
    private TableColumn<Artist, String> cityColumn;
    @FXML
    private TableColumn<Artist, String> emailColumn;
    @FXML
    private TableColumn<Artist, Integer> yearColumn;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));

        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        Discipline d = disciplineFilter.getValue();
        String dName = (d != null) ? d.getName() : null;
        artistTable.setItems(FXCollections.observableArrayList(artistService.searchArtists(query, dName, null)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        disciplineFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleAddArtist() {
        Dialog<Artist> dialog = createArtistDialog(null);
        dialog.showAndWait().ifPresent(artist -> {
            artistService.createArtist(artist);
            refreshTable();
        });
    }

    @FXML
    private void handleEditArtist() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an artist to edit.");
            return;
        }

        Dialog<Artist> dialog = createArtistDialog(selected);
        dialog.showAndWait().ifPresent(artist -> {
            artistService.updateArtist(artist);
            refreshTable();
        });
    }

    @FXML
    private void handleDeleteArtist() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an artist to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete " + selected.getName() + "?");
        confirm.setContentText("Are you sure you want to delete this artist?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                artistService.deleteArtist(selected.getName());
                refreshTable();
            }
        });
    }

    private Dialog<Artist> createArtistDialog(Artist existingArtist) {
        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle(existingArtist == null ? "Add New Artist" : "Edit Artist");
        dialog.setHeaderText("Enter artist details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField cityField = new TextField();
        cityField.setPromptText("City");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField yearField = new TextField();
        yearField.setPromptText("Birth Year");
        TextField bioField = new TextField();
        bioField.setPromptText("Bio");

        if (existingArtist != null) {
            nameField.setText(existingArtist.getName());
            nameField.setDisable(true); // primary key usually shouldn't change
            cityField.setText(existingArtist.getCity());
            emailField.setText(existingArtist.getContactEmail());
            yearField.setText(existingArtist.getBirthYear() != null ? existingArtist.getBirthYear().toString() : "");
            bioField.setText(existingArtist.getBio());
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("City:"), 0, 1);
        grid.add(cityField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Birth Year:"), 0, 3);
        grid.add(yearField, 1, 3);
        grid.add(new Label("Bio:"), 0, 4);
        grid.add(bioField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Integer year = null;
                try {
                    year = !yearField.getText().isEmpty() ? Integer.parseInt(yearField.getText()) : null;
                } catch (NumberFormatException e) {
                    // Ignore or handle
                }
                Artist artist = new Artist(nameField.getText(), bioField.getText(), year, emailField.getText(), cityField.getText());
                // Preserving existing state if we are editing
                if (existingArtist != null) {
                    artist.setActive(existingArtist.isActive());
                    artist.setPhone(existingArtist.getPhone());
                    artist.setWebsite(existingArtist.getWebsite());
                    artist.setSocialMedia(existingArtist.getSocialMedia());
                }
                return artist;
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

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }
}

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
    @FXML private TextField searchField;
    @FXML private ComboBox<Discipline> disciplineFilter;
    @FXML private TableView<Artist> artistTable;
    @FXML private TableColumn<Artist, String> nameColumn;
    @FXML private TableColumn<Artist, String> cityColumn;
    @FXML private TableColumn<Artist, String> emailColumn;
    @FXML private TableColumn<Artist, String> disciplineColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        disciplineColumn.setCellValueFactory(cellData -> {
            Artist a = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    a.getDisciplines() != null && !a.getDisciplines().isEmpty() ? a.getDisciplines().get(0).getName() : "");
        });
        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        refreshTable();
    }

    public void updateButtonVisibility() {
        // Buttons always visible — permission check happens in click handler
    }

    private MainController getMainController() {
        return ServiceProvider.getMainController();
    }

    public void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }

    @FXML
    private void handleAddArtist() {
        MainController main = getMainController();
        if (main == null || !main.isLoggedIn()) {
            showAlert("Access Denied", "You must be logged in to add an artist.");
            return;
        }
        Dialog<Artist> dialog = createArtistDialog(null);
        dialog.showAndWait().ifPresent(artist -> {
            artistService.createArtist(artist);
            refreshTable();
        });
    }

    @FXML
    private void handleEditArtist() {
        MainController main = getMainController();
        if (main == null || !main.isAdmin()) {
            showAlert("Access Denied", "Only admins can edit artists.");
            return;
        }
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("No Selection", "Please select an artist to edit."); return; }
        Dialog<Artist> dialog = createArtistDialog(selected);
        dialog.showAndWait().ifPresent(artist -> {
            artistService.updateArtist(artist);
            refreshTable();
        });
    }

    @FXML
    private void handleDeleteArtist() {
        MainController main = getMainController();
        if (main == null || !main.isAdmin()) {
            showAlert("Access Denied", "Only admins can delete artists.");
            return;
        }
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("No Selection", "Please select an artist to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete \"" + selected.getName() + "\"?");
        confirm.setContentText("This will also delete associated artworks.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    artistService.deleteArtist(selected.getId());
                    refreshTable();
                } catch (RuntimeException ex) {
                    showAlert("Cannot Delete", ex.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim();
        String discName = disciplineFilter.getValue() != null ? disciplineFilter.getValue().getName() : "";
        if (query.isEmpty() && discName.isEmpty()) {
            refreshTable();
        } else {
            artistTable.setItems(FXCollections.observableArrayList(
                    artistService.searchArtists(query, discName, "")));
        }
    }

    private Dialog<Artist> createArtistDialog(Artist existing) {
        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New Artist" : "Edit Artist");
        dialog.setHeaderText("Enter artist details");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField(); nameField.setPromptText("Name *");
        TextField cityField = new TextField(); cityField.setPromptText("City");
        TextField emailField = new TextField(); emailField.setPromptText("Email (e.g. name@example.com)");
        TextField yearField = new TextField(); yearField.setPromptText("Birth Year (e.g. 1985)");
        TextField phoneField = new TextField(); phoneField.setPromptText("Phone (e.g. 0612345678)");
        TextField websiteField = new TextField(); websiteField.setPromptText("Website");
        TextField bioField = new TextField(); bioField.setPromptText("Bio");

        if (existing != null) {
            nameField.setText(existing.getName()); nameField.setDisable(true);
            cityField.setText(existing.getCity());
            emailField.setText(existing.getContactEmail());
            if (existing.getBirthYear() != null) yearField.setText(existing.getBirthYear().toString());
            phoneField.setText(existing.getPhone());
            websiteField.setText(existing.getWebsite());
            bioField.setText(existing.getBio());
        }

        grid.add(new Label("Name *:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("City:"), 0, 1); grid.add(cityField, 1, 1);
        grid.add(new Label("Email:"), 0, 2); grid.add(emailField, 1, 2);
        grid.add(new Label("Birth Year:"), 0, 3); grid.add(yearField, 1, 3);
        grid.add(new Label("Phone:"), 0, 4); grid.add(phoneField, 1, 4);
        grid.add(new Label("Website:"), 0, 5); grid.add(websiteField, 1, 5);
        grid.add(new Label("Bio:"), 0, 6); grid.add(bioField, 1, 6);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(db -> {
            if (db == saveBtn) {
                String name = nameField.getText().trim();
                String yearStr = yearField.getText().trim();
                String email = emailField.getText().trim();

                if (name.isEmpty()) { showAlert("Validation Error", "Name is required."); return null; }
                if (!email.isEmpty() && (!email.contains("@") || !email.contains("."))) {
                    showAlert("Validation Error", "Please enter a valid email."); return null; }
                if (!yearStr.isEmpty()) {
                    try {
                        int y = Integer.parseInt(yearStr);
                        if (y < 1900 || y > 2010) { showAlert("Validation Error", "Birth year must be between 1900 and 2010."); return null; }
                    } catch (NumberFormatException e) { showAlert("Validation Error", "Birth year must be a number."); return null; }
                }

                Artist a = new Artist(name, bioField.getText().trim(), yearStr.isEmpty() ? null : Integer.parseInt(yearStr), email, cityField.getText().trim());
                a.setPhone(phoneField.getText().trim());
                a.setWebsite(websiteField.getText().trim());
                a.setBio(bioField.getText().trim());
                if (existing != null) {
                a.setId(existing.getId());
                a.setDisciplines(existing.getDisciplines());
            }
                return a;
            }
            return null;
        });
        return dialog;
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}

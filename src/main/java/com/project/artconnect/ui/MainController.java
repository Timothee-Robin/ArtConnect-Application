package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.util.ServiceProvider;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

public class MainController {
    @FXML
    private TabPane mainTabPane;

    @FXML
    private Label statusLabel;
    
    @FXML
    private Label userLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Button logoutButton;

    @FXML private DiscoverController discoverTabController;
    @FXML private ArtistController artistsTabController;
    @FXML private ArtworkController artworksTabController;
    @FXML private GalleryController galleriesTabController;
    @FXML private ExhibitionController exhibitionsTabController;
    @FXML private WorkshopController workshopsTabController;
    @FXML private CommunityController communityTabController;

    private String currentUser = null;
    private String currentRole = null;

    @FXML
    public void initialize() {
        // Display the current data mode (Supabase or In-Memory) in the status bar
        statusLabel.setText("ArtConnect Pro v1.0 | Mode: " + ServiceProvider.getModeName());
        updateUserUI();
    }

    @FXML
    private void handleLogin() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.setHeaderText("Enter credentials");

        ButtonType loginBtnType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginBtnType) {
                return new String[]{username.getText(), password.getText()};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(credentials -> {
            ServiceProvider.getCommunityService()
                .authenticate(credentials[0], credentials[1])
                .ifPresentOrElse(user -> {
                    currentUser = user.getName();
                    currentRole = "user"; // Simplified for CommunityMember
                    updateUserUI();
                }, () -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid username or password.");
                    alert.showAndWait();
                });
        });
    }

    @FXML
    private void handleRegister() {
        Dialog<CommunityMember> dialog = new Dialog<>();
        dialog.setTitle("Register");
        dialog.setHeaderText("Create an account (Community Member)");

        ButtonType registerBtnType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name (Username)");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField yearField = new TextField();
        yearField.setPromptText("Birth Year (Optional)");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone (Optional)");
        TextField cityField = new TextField();
        cityField.setPromptText("City (Optional)");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Birth Year:"), 0, 3);
        grid.add(yearField, 1, 3);
        grid.add(new Label("Phone:"), 0, 4);
        grid.add(phoneField, 1, 4);
        grid.add(new Label("City:"), 0, 5);
        grid.add(cityField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerBtnType) {
                if (nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()) {
                    return null; // Require at least name, email, password
                }
                CommunityMember member = new CommunityMember(nameField.getText().trim(), emailField.getText().trim());
                member.setPassword(passwordField.getText());
                if (!yearField.getText().trim().isEmpty()) {
                    try {
                        member.setBirthYear(Integer.parseInt(yearField.getText().trim()));
                    } catch (NumberFormatException ignored) {}
                }
                member.setPhone(phoneField.getText().trim());
                member.setCity(cityField.getText().trim());
                member.setMembershipType("free"); // Default
                return member;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(member -> {
            boolean success = ServiceProvider.getCommunityService().register(member);
            if (success) {
                Alert msg = new Alert(Alert.AlertType.INFORMATION);
                msg.setContentText("Registered successfully! You can now log in.");
                msg.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Username (Name) already taken. Please choose another.");
                alert.showAndWait();
            }
        });
    }

    @FXML
    private void handleLogout() {
        currentUser = null;
        currentRole = null;
        updateUserUI();
    }

    @FXML
    private void handleRefresh() {
        if (discoverTabController != null) discoverTabController.refreshTable();
        if (artistsTabController != null) artistsTabController.refreshTable();
        if (artworksTabController != null) artworksTabController.refreshTable();
        if (galleriesTabController != null) galleriesTabController.refreshTable();
        if (exhibitionsTabController != null) exhibitionsTabController.refreshTable();
        if (workshopsTabController != null) workshopsTabController.refreshTable();
        if (communityTabController != null) communityTabController.refreshTable();

        statusLabel.setText("ArtConnect Pro v1.0 | Mode: " + ServiceProvider.getModeName() + " (Data Refreshed)");
    }

    private void updateUserUI() {
        if (currentUser != null) {
            userLabel.setText("Logged in as: " + currentUser + " (" + currentRole + ")");
            loginButton.setVisible(false);
            loginButton.setManaged(false);
            registerButton.setVisible(false);
            registerButton.setManaged(false);
            logoutButton.setVisible(true);
            logoutButton.setManaged(true);
        } else {
            userLabel.setText("Not logged in");
            loginButton.setVisible(true);
            loginButton.setManaged(true);
            registerButton.setVisible(true);
            registerButton.setManaged(true);
            logoutButton.setVisible(false);
            logoutButton.setManaged(false);
        }
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }
}

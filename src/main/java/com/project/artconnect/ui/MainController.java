package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.util.ServiceProvider;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.util.Optional;

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
    private CommunityMember loggedInMember = null;

    // Getters so sub-controllers can check permissions
    public String getCurrentUser() { return currentUser; }
    public String getCurrentRole() { return currentRole; }
    public boolean isLoggedIn() { return currentUser != null; }
    public boolean isAdmin() { return "admin".equals(currentRole); }

    @FXML
    public void initialize() {
        ServiceProvider.setMainController(this);
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
            if (credentials[0].trim().isEmpty() || credentials[1].trim().isEmpty()) {
                showError("Please enter both username and password.");
                return;
            }
            Optional<CommunityMember> user = ServiceProvider.getCommunityService()
                .authenticate(credentials[0].trim(), credentials[1]);
            user.ifPresentOrElse(m -> {
                currentUser = m.getName();
                currentRole = m.getRole();
                loggedInMember = m;
                updateUserUI();
            }, () -> showError("Invalid username or password."));
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
        yearField.setPromptText("Birth Year (e.g. 1995)");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Birth Year:"), 0, 3);
        grid.add(yearField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerBtnType) {
                // Validation
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String password = passwordField.getText();
                String yearStr = yearField.getText().trim();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    showError("Name, Email and Password are required.");
                    return null;
                }
                if (password.length() < 6) {
                    showError("Password must be at least 6 characters.");
                    return null;
                }
                if (!email.contains("@") || !email.contains(".")) {
                    showError("Please enter a valid email address.");
                    return null;
                }
                if (!yearStr.isEmpty()) {
                    try {
                        int year = Integer.parseInt(yearStr);
                        if (year < 1900 || year > 2010) {
                            showError("Birth year must be between 1900 and 2010.");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showError("Birth year must be a valid number.");
                        return null;
                    }
                }

                CommunityMember member = new CommunityMember(name, email);
                member.setPassword(password);
                if (!yearStr.isEmpty()) {
                    member.setBirthYear(Integer.parseInt(yearStr));
                }
                member.setRole("user");
                return member;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(member -> {
            boolean success = ServiceProvider.getCommunityService().register(member);
            if (success) {
                showInfo("Registered successfully! You can now log in.");
            } else {
                showError("Username (Name) already taken. Please choose another.");
            }
        });
    }

    @FXML
    private void handleLogout() {
        currentUser = null;
        currentRole = null;
        loggedInMember = null;
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }
}
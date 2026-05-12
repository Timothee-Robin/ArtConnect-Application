package com.project.artconnect.ui;

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
            // Mock authentication check for now
            // Ideally, you'd use UserService.authenticate(credentials[0], credentials[1])
            String user = credentials[0];
            if (!user.isEmpty()) {
                currentUser = user;
                currentRole = "admin".equals(user) ? "admin" : "user";
                updateUserUI();
            }
        });
    }

    @FXML
    private void handleRegister() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Register");
        dialog.setHeaderText("Create an account");

        ButtonType registerBtnType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("user", "admin");
        roleCombo.setValue("user");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        grid.add(new Label("Role:"), 0, 2);
        grid.add(roleCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerBtnType) {
                return new String[]{username.getText(), password.getText(), roleCombo.getValue()};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(credentials -> {
            // Mock registration for now
            // Ideally: UserService.register(credentials[0], credentials[1], credentials[2])
            Alert msg = new Alert(Alert.AlertType.INFORMATION);
            msg.setContentText("Registered user: " + credentials[0] + " with role: " + credentials[2]);
            msg.showAndWait();
        });
    }

    @FXML
    private void handleLogout() {
        currentUser = null;
        currentRole = null;
        updateUserUI();
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

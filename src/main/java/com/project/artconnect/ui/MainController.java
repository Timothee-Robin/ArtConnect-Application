package com.project.artconnect.ui;

import com.project.artconnect.util.ServiceProvider;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.application.Platform;

public class MainController {
    @FXML
    private TabPane mainTabPane;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        // Display the current data mode (Supabase or In-Memory) in the status bar
        statusLabel.setText("ArtConnect Pro v1.0 | Mode: " + ServiceProvider.getModeName());
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }
}

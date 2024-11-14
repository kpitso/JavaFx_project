package com.example.sql_connect;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Parent;

public class HomeController {

    @FXML
    private Button btnAdmin;

    @FXML
    private Button btnLecturer;

    @FXML
    private Button btnPrl;

    // Method to switch to the Login screen
    private void switchToLogin() throws IOException {
        // Load the Login.fxml file
        Parent loginView = FXMLLoader.load(getClass().getResource("Login.fxml"));

        // Create a new Scene with the loaded layout
        Scene loginScene = new Scene(loginView);

        // Get the current stage (window)
        Stage currentStage = (Stage) btnAdmin.getScene().getWindow();

        // Set the new Scene to the current stage
        currentStage.setScene(loginScene);
        currentStage.show();
    }

    @FXML
    private void openAdmin() throws IOException {
        switchToLogin();
    }

    @FXML
    private void openLecturer() throws IOException {

    }

    @FXML
    private void openPrl() throws IOException {
        switchToLogin();
    }

    public void signup(ActionEvent actionEvent) {
    }
}
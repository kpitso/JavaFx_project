package com.example.sql_connect;

import com.example.sql_connect.sqlConnection; // Ensure this import is correct
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private sqlConnection database;

    public LoginController() {
        database = new sqlConnection(); // Initialize the database connection
    }

    // Handles login action and loads the appropriate scene based on role
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        String userRole = authenticateUser(username, password);
        if (userRole != null) {
            loadScene(userRole, username); // Pass username to load scene for profile display
        } else {
            displayErrorMessage("Invalid username or password."); // Show error message in dialog
        }
    }

    // Authenticates the user by querying the database and returns role if successful
    private String authenticateUser(String username, String password) {
        String query = "SELECT 'admin' AS role FROM admin WHERE username=? AND password=? "
                + "UNION "
                + "SELECT 'lecturer' AS role FROM lecturer WHERE username=? AND password=? "
                + "UNION "
                + "SELECT 'prl' AS role FROM prl WHERE username=? AND password=?";

        try (Connection conn = database.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, username);
            pst.setString(4, password);
            pst.setString(5, username);
            pst.setString(6, password);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getString("role"); // Returns the role if user is found
            }

        } catch (Exception e) {
            displayErrorMessage("Database error: " + e.getMessage()); // Show database error
            e.printStackTrace();
        }
        return null; // Return null if no user found
    }

    // Loads the appropriate scene based on role and displays the profile view for 'admin'
    private void loadScene(String role, String username) {
        String fxmlToLoad;
        switch (role) {
            case "admin":
                fxmlToLoad = "admin.fxml"; // Make sure this path and controller are correct
                break;
            case "lecturer":
                fxmlToLoad = "lecturer.fxml";
                break;
            case "prl":
                fxmlToLoad = "prl.fxml";
                break;
            default:
                displayErrorMessage("Invalid role."); // Show invalid role message
                return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlToLoad));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);

            // Load profile details only if it's the admin scene and if admin profile viewing is needed
            if ("admin".equals(role) && loader.getController() instanceof profileController profileController) {
                profileController.loadProfile(username);
            }

            stage.show();
        } catch (IOException e) {
            displayErrorMessage("Failed to load the next scene."); // Show scene load failure message
            e.printStackTrace();
        }
    }

    // Cancel action
    @FXML
    private void cancel(ActionEvent actionEvent) {
        // Implement cancel action if needed
    }

    // Display error message in a pop-up dialog
    private void displayErrorMessage(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
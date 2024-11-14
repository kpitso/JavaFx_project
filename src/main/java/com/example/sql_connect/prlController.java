package com.example.sql_connect;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class prlController {

    @FXML
    private StackPane formContainer;

    private sqlConnection dbConnection;

    public prlController() {
        dbConnection = new sqlConnection();
    }

    @FXML
    public void onFillReportButtonClick() {
        // Create the form layout
        VBox formLayout = new VBox(10);
        formLayout.setStyle("-fx-padding: 20; -fx-background-color: #FFFFFF;");

        // Class selection
        Label classLabel = new Label("Select Class:");
        ComboBox<String> classComboBox = new ComboBox<>();
        populateClassComboBox(classComboBox);

        // Module selection
        Label moduleLabel = new Label("Select Module:");
        ComboBox<String> moduleComboBox = new ComboBox<>();
        classComboBox.setOnAction(event -> populateModuleComboBox(classComboBox.getSelectionModel().getSelectedItem(), moduleComboBox));

        // Challenges and recommendations text areas
        Label challengesLabel = new Label("Challenges:");
        TextArea challengesTextArea = new TextArea();
        challengesTextArea.setPromptText("Enter challenges here...");

        Label recommendationsLabel = new Label("Recommendations:");
        TextArea recommendationsTextArea = new TextArea();
        recommendationsTextArea.setPromptText("Enter recommendations here...");

        // Submit button
        Button submitButton = new Button("Submit Report");
        submitButton.setOnAction(event -> {
            submitReport(classComboBox, moduleComboBox, challengesTextArea.getText(), recommendationsTextArea.getText());
        });

        // Add all components to the layout
        formLayout.getChildren().addAll(
                classLabel, classComboBox,
                moduleLabel, moduleComboBox,
                challengesLabel, challengesTextArea,
                recommendationsLabel, recommendationsTextArea,
                submitButton
        );

        // Clear any existing components and add the new form
        formContainer.getChildren().clear();
        formContainer.getChildren().add(formLayout);
    }

    private void populateClassComboBox(ComboBox<String> classComboBox) {
        Connection connection = dbConnection.getConnection();
        String query = "SELECT class_name FROM classes";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                classComboBox.getItems().add(resultSet.getString("class_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error retrieving classes.");
        }
    }

    private void populateModuleComboBox(String selectedClass, ComboBox<String> moduleComboBox) {
        moduleComboBox.getItems().clear(); // Clear previous items

        if (selectedClass != null) {
            Connection connection = dbConnection.getConnection();
            String query = "SELECT m.module_name FROM modules m " +
                    "JOIN classes c ON m.semester_id = c.id " +
                    "WHERE c.class_name = ?";

            try {
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, selectedClass);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    moduleComboBox.getItems().add(resultSet.getString("module_name"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error retrieving modules for the selected class.");
            }
        }
    }

    private void submitReport(ComboBox<String> classComboBox, ComboBox<String> moduleComboBox, String challenges, String recommendations) {
        String selectedClass = classComboBox.getSelectionModel().getSelectedItem();
        String selectedModule = moduleComboBox.getSelectionModel().getSelectedItem();

        if (selectedClass == null || selectedModule == null || challenges.isEmpty() || recommendations.isEmpty()) {
            showAlert("Please fill all fields.");
            return;
        }

        int classId = getIdByName("classes", selectedClass);
        int moduleId = getIdByName("modules", selectedModule);

        if (classId != -1 && moduleId != -1) {
            saveReport(classId, moduleId, challenges, recommendations);
        } else {
            showAlert("Invalid class or module selected.");
        }
    }

    private int getIdByName(String tableName, String name) {
        Connection connection = dbConnection.getConnection();
        String query = "SELECT id FROM " + tableName + " WHERE ";

        if (tableName.equals("classes")) {
            query += "class_name = ?";
        } else if (tableName.equals("modules")) {
            query += "module_name = ?";
        }

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // not found
    }

    private void saveReport(int classId, int moduleId, String challenges, String recommendations) {
        Connection connection = dbConnection.getConnection();
        String query = "INSERT INTO reports (class_id, module_id, challenges, recommendations) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, classId);
            statement.setInt(2, moduleId);
            statement.setString(3, challenges);
            statement.setString(4, recommendations);
            statement.executeUpdate();
            showAlert("Report saved successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error saving report.");
        }
    }

    // Modified Method to View Reports
    public void onViewReportButtonClick(ActionEvent actionEvent) {
        StringBuilder reportData = new StringBuilder();
        Connection connection = dbConnection.getConnection();
        String query = "SELECT * FROM reports";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int classId = resultSet.getInt("class_id");
                int moduleId = resultSet.getInt("module_id");
                String challenges = resultSet.getString("challenges");
                String recommendations = resultSet.getString("recommendations");

                reportData.append("ID: ").append(id)
                        .append(", Class ID: ").append(classId)
                        .append(", Module ID: ").append(moduleId)
                        .append(", Challenges: ").append(challenges)
                        .append(", Recommendations: ").append(recommendations)
                        .append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error retrieving reports.");
        }

        // Show the report data in an alert
        if (reportData.length() == 0) {
            showAlert("No reports found.");
        } else {
            showAlert(reportData.toString());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Report Data");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleLogout(ActionEvent actionEvent) {
        Stage stage = (Stage) formContainer.getScene().getWindow();
        try {
            // Load the home.fxml scene
            Parent homePageParent = FXMLLoader.load(getClass().getResource("home.fxml"));
            Scene homePageScene = new Scene(homePageParent);

            // Set the scene and show the window
            stage.setScene(homePageScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the error, if any
        }
    }

    public void handleExit(ActionEvent actionEvent) {
        System.exit(0);
    }
}
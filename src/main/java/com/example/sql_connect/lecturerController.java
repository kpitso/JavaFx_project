package com.example.sql_connect;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class lecturerController {

    // FXML elements
    @FXML
    private StackPane formContainer;  // Container to hold the forms
    @FXML
    private ComboBox<String> studentComboBox;  // ComboBox for selecting student
    @FXML
    private ComboBox<String> classComboBox;    // ComboBox for selecting class
    @FXML
    private ComboBox<String> statusComboBox;   // ComboBox for selecting attendance status
    @FXML
    private TextField chapterField;            // TextField for chapter input
    @FXML
    private TextField learningOutcomeField;    // TextField for learning outcomes input
    @FXML
    private Label messageLabel;                 // Label to display messages

    // Initialize method to load data
    @FXML
    public void initialize() {
        loadStudents();
        loadClasses();
        // Set attendance status options
        statusComboBox.setItems(FXCollections.observableArrayList("present", "absent"));
    }

    // Load students into the ComboBox
    private void loadStudents() {
        ObservableList<String> studentList = FXCollections.observableArrayList();
        String query = "SELECT student_number FROM student";
        try (Connection conn = new sqlConnection().getConnection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                studentList.add(rs.getString("student_number"));
            }
            studentComboBox.setItems(studentList);
        } catch (SQLException e) {
            displayErrorMessage("Error loading students: " + e.getMessage());
        }
    }

    // Load classes into the ComboBox
    private void loadClasses() {
        ObservableList<String> classList = FXCollections.observableArrayList();
        String query = "SELECT class_name FROM classes";
        try (Connection conn = new sqlConnection().getConnection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                classList.add(rs.getString("class_name"));
            }
            classComboBox.setItems(classList);
        } catch (SQLException e) {
            displayErrorMessage("Error loading classes: " + e.getMessage());
        }
    }

    // Handle Mark Attendance button action
    @FXML
    private void handleMarkAttendance() {
        clearFormContainer();
        displayAttendanceForm();
    }

    // Handle Specify Chapter and Outcomes button action
    @FXML
    private void handleSpecifyChapterAndOutcomes() {
        clearFormContainer();
        displayChapterForm();
    }

    // Clear the form container
    private void clearFormContainer() {
        formContainer.getChildren().clear();
        messageLabel.setText("");
    }

    // Display the attendance form
    private void displayAttendanceForm() {
        Label formLabel = new Label("Mark Attendance");
        formLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #000000;");

        VBox formLayout = new VBox(10);

        HBox studentBox = new HBox(10);
        studentBox.getChildren().addAll(new Label("Student: "), studentComboBox);

        HBox classBox = new HBox(10);
        classBox.getChildren().addAll(new Label("Class: "), classComboBox);

        HBox statusBox = new HBox(10);
        statusBox.getChildren().addAll(new Label("Status: "), statusComboBox);

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> handleSubmitAttendance());

        formLayout.getChildren().addAll(
                formLabel,
                studentBox,
                classBox,
                statusBox,
                submitButton,
                messageLabel
        );

        formContainer.getChildren().add(formLayout);
    }

    // Display the chapter and outcomes form
    private void displayChapterForm() {
        Label formLabel = new Label("Specify Chapter and Learning Outcomes");
        formLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #000000;");

        VBox formLayout = new VBox(10);

        HBox chapterBox = new HBox(10);
        chapterBox.getChildren().addAll(new Label("Chapter: "), chapterField);

        HBox outcomeBox = new HBox(10);
        outcomeBox.getChildren().addAll(new Label("Learning Outcomes: "), learningOutcomeField);

        // Class selection for chapter and outcomes
        HBox classBox = new HBox(10);
        classBox.getChildren().addAll(new Label("Class: "), classComboBox);

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> handleSubmitChapterAndOutcomes());

        formLayout.getChildren().addAll(
                formLabel,
                classBox,
                chapterBox,
                outcomeBox,
                submitButton,
                messageLabel
        );

        formContainer.getChildren().add(formLayout);
    }

    // Submit attendance
    private void handleSubmitAttendance() {
        String studentNumber = studentComboBox.getSelectionModel().getSelectedItem();
        String className = classComboBox.getSelectionModel().getSelectedItem();
        String status = statusComboBox.getSelectionModel().getSelectedItem();

        if (studentNumber == null || className == null || status == null) {
            displayErrorMessage("Please fill in all fields.");
            return;
        }

        if (markAttendance(studentNumber, className, status)) {
            displaySuccessMessage("Attendance marked successfully.");
        } else {
            displayErrorMessage("Failed to mark attendance.");
        }
    }

    // Submit chapter and outcomes
    private void handleSubmitChapterAndOutcomes() {
        String chapter = chapterField.getText();
        String learningOutcomes = learningOutcomeField.getText();
        String selectedClass = classComboBox.getSelectionModel().getSelectedItem();

        if (chapter.isEmpty() || learningOutcomes.isEmpty() || selectedClass == null) {
            displayErrorMessage("Please fill in all fields.");
            return;
        }

        if (saveChapterAndOutcomes(selectedClass, chapter, learningOutcomes)) {
            displaySuccessMessage("Chapter and outcomes saved successfully.");
            chapterField.clear();
            learningOutcomeField.clear();
        } else {
            displayErrorMessage("Failed to save chapter and outcomes.");
        }
    }

    // Save chapter and learning outcomes in the database
    private boolean saveChapterAndOutcomes(String className, String chapter, String learningOutcomes) {
        String query = "INSERT INTO class (ClassCode, ClassName, Chapter, LearningOutcome) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE Chapter=?, LearningOutcome=?";
        try (Connection conn = new sqlConnection().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, "2024"); // Set ClassCode accordingly; modify as needed
            pst.setString(2, className);
            pst.setString(3, chapter);
            pst.setString(4, learningOutcomes);
            pst.setString(5, chapter);  // For update
            pst.setString(6, learningOutcomes); // For update
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            displayErrorMessage("Error saving chapter and outcomes: " + e.getMessage());
            return false;
        }
    }

    // Mark attendance in the database
    private boolean markAttendance(String studentNumber, String className, String status) {
        int studentId = getStudentId(studentNumber);
        int classId = getClassId(className);

        if (studentId == -1 || classId == -1) {
            displayErrorMessage("Invalid student or class selection.");
            return false;
        }

        String query = "INSERT INTO attendance (student_id, class_id, date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = new sqlConnection().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, studentId);
            pst.setInt(2, classId);
            pst.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            pst.setString(4, status);
            pst.executeUpdate();
            return true;
        } catch (SQLException e) {
            displayErrorMessage("Error marking attendance: " + e.getMessage());
            return false;
        }
    }

    // Retrieve student ID based on student number
    private int getStudentId(String studentNumber) {
        String query = "SELECT id FROM student WHERE student_number = ?";
        try (Connection conn = new sqlConnection().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, studentNumber);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            displayErrorMessage("Error retrieving student ID: " + e.getMessage());
        }
        return -1; // Not found
    }

    // Retrieve class ID based on class name
    private int getClassId(String className) {
        String query = "SELECT id FROM classes WHERE class_name = ?";
        try (Connection conn = new sqlConnection().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, className);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            displayErrorMessage("Error retrieving class ID: " + e.getMessage());
        }
        return -1; // Not found
    }

    // Display error message in a pop-up dialog
    private void displayErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null); // No header
        alert.setContentText(message); // Set the error message
        alert.showAndWait(); // Display the dialog
    }

    // Display success message in a pop-up dialog
    private void displaySuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null); // No header
        alert.setContentText(message); // Set the success message
        alert.showAndWait(); // Display the dialog
    }

    // Handle Logout button action
    @FXML
    private void handleLogout() {
        Stage stage = (Stage) formContainer.getScene().getWindow();
        try {
            // Load the home.fxml scene
            Parent homePageParent = FXMLLoader.load(getClass().getResource("home.fxml"));
            Scene homePageScene = new Scene(homePageParent);

            // Set the scene and show the window
            stage.setScene(homePageScene);
            stage.show();
        } catch (IOException e) {
            displayErrorMessage("Error loading home page: " + e.getMessage());
        }
    }

    // Handle Clear Form button action
    @FXML
    private void handleClearForm() {
        studentComboBox.getSelectionModel().clearSelection();
        classComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
        chapterField.clear();
        learningOutcomeField.clear();
        messageLabel.setText("");
    }

    // Handle Exit button action
    @FXML
    private void handleExit() {
        System.exit(0);
    }

    public void handleViewPreviousAttendance(javafx.event.ActionEvent actionEvent) {
    }
}
package com.example.sql_connect;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class adminController {

    @FXML
    private StackPane formContainer; // This will hold the forms

    // Instance of sqlConnection to manage database connections
    private sqlConnection database;
    private String currentUsername; // Store the username of the logged-in admin

    public adminController() {
        database = new sqlConnection(); // Initialize the database connection instance
    }

    // Method to set the current username for profile fetching
    public void setUsername(String username) {
        this.currentUsername = username;
    }

    @FXML
    private void showProfileForm() {
        clearFormContainer();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Profile.fxml"));
            Parent profileRoot = loader.load();

            profileController profileController = loader.getController();
            profileController.loadProfile(currentUsername); // Pass the current username to load profile

            // Instead of setting a new scene, just add the profileRoot to the formContainer
            formContainer.getChildren().add(profileRoot);
        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Failed to load profile view: " + e.getMessage());
        }
    }

    @FXML
    private void showAddLecturerForm() {
        clearFormContainer();
        VBox addLecturerForm = new VBox(10);
        TextField lecturerUsername = new TextField();
        lecturerUsername.setPromptText("Lecturer Username");
        TextField lecturerPassword = new TextField();
        lecturerPassword.setPromptText("Lecturer Password");
        TextField lecturerName = new TextField();
        lecturerName.setPromptText("Lecturer Name");
        TextField employeeNumber = new TextField();
        employeeNumber.setPromptText("Employee Number");

        Button addButton = new Button("Add Lecturer");
        addButton.setOnAction(e -> {
            addLecturer(lecturerUsername.getText(), lecturerPassword.getText(), lecturerName.getText(), employeeNumber.getText());
        });

        addLecturerForm.getChildren().addAll(lecturerUsername, lecturerPassword, lecturerName, employeeNumber, addButton);
        formContainer.getChildren().add(addLecturerForm);
    }

    // Method to add lecturer details to the database using sqlConnection
    private void addLecturer(String username, String password, String name, String employeeNum) {
        String query = "INSERT INTO lecturer (username, password, name, employee_number) VALUES (?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, name);
            statement.setString(4, employeeNum);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                displaySuccessMessage("A new lecturer was inserted successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error inserting lecturer: " + e.getMessage());
        }
    }

    @FXML
    private void showAddAcademicYearForm() {
        clearFormContainer();
        VBox addAcademicYearForm = new VBox(10);
        TextField academicYear = new TextField();
        academicYear.setPromptText("Academic Year (e.g., 2023)");

        Button addButton = new Button("Add Academic Year");
        addButton.setOnAction(e -> {
            addAcademicYear(academicYear.getText());
        });

        addAcademicYearForm.getChildren().addAll(academicYear, addButton);
        formContainer.getChildren().add(addAcademicYearForm);
    }

    // Add academic year to the database
    private void addAcademicYear(String yearName) {
        String query = "INSERT INTO academic_years (year_name) VALUES (?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, yearName);
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                displaySuccessMessage("A new academic year was inserted successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error inserting academic year: " + e.getMessage());
        }
    }

    @FXML
    private void showAddSemesterForm() {
        clearFormContainer();
        VBox addSemesterForm = new VBox(10);

        TextField semesterName = new TextField();
        semesterName.setPromptText("Semester Name");

        ComboBox<String> academicYearComboBox = new ComboBox<>();
        academicYearComboBox.getItems().addAll(getAcademicYears());
        academicYearComboBox.setPromptText("Select Academic Year");

        TextField newAcademicYearField = new TextField();
        newAcademicYearField.setPromptText("Enter New Academic Year");

        Button addButton = new Button("Add Semester");
        addButton.setOnAction(e -> {
            String selectedAcademicYearId = academicYearComboBox.getValue();
            String newAcademicYearName = newAcademicYearField.getText();
            if (selectedAcademicYearId != null && !semesterName.getText().isEmpty()) {
                addSemester(semesterName.getText(), selectedAcademicYearId.split(": ")[0]);
            } else if (!newAcademicYearName.isEmpty() && !semesterName.getText().isEmpty()) {
                addAcademicYear(newAcademicYearName); // Add new academic year
                List<String> updatedYears = getAcademicYears(); // Refresh combo box
                academicYearComboBox.getItems().clear();
                academicYearComboBox.getItems().addAll(updatedYears);
                academicYearComboBox.setValue(newAcademicYearName); // Select the new academic year
                addSemester(semesterName.getText(), newAcademicYearName); // Add semester with the new academic year
            } else {
                displayErrorMessage("Please enter a semester name and select/enter an academic year.");
            }
        });

        addSemesterForm.getChildren().addAll(semesterName, academicYearComboBox, newAcademicYearField, addButton);
        formContainer.getChildren().add(addSemesterForm);
    }

    // Method to retrieve academic years from the database
    private List<String> getAcademicYears() {
        List<String> academicYears = new ArrayList<>();
        String query = "SELECT id, year_name FROM academic_years";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                academicYears.add(resultSet.getString("id") + ": " + resultSet.getString("year_name")); // Display "id: year_name"
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error retrieving academic years: " + e.getMessage());
        }

        return academicYears;
    }

    // Method to add semester details to the database using sqlConnection
    private void addSemester(String semesterName, String academicYearId) {
        String query = "INSERT INTO semesters (semester_name, academic_year_id) VALUES (?, ?)";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, semesterName);
            statement.setString(2, academicYearId);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                displaySuccessMessage("A new semester was inserted successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error inserting semester: " + e.getMessage());
        }
    }

    @FXML
    private void showAddModuleForm() {
        clearFormContainer();
        VBox addModuleForm = new VBox(10);
        TextField moduleName = new TextField();
        moduleName.setPromptText("Module Name");

        ComboBox<String> semesterComboBox = new ComboBox<>();
        semesterComboBox.getItems().addAll(getSemesters());
        semesterComboBox.setPromptText("Select Semester");

        Button addButton = new Button("Add Module");
        addButton.setOnAction(e -> {
            String semesterId = semesterComboBox.getValue();
            if (semesterId != null && !moduleName.getText().isEmpty()) {
                addModule(moduleName.getText(), semesterId.split(": ")[0]);
            } else {
                displayErrorMessage("Please enter a module name and select a semester.");
            }
        });

        addModuleForm.getChildren().addAll(moduleName, semesterComboBox, addButton);
        formContainer.getChildren().add(addModuleForm);
    }

    // Method to retrieve semesters from the database
    private List<String> getSemesters() {
        List<String> semesters = new ArrayList<>();
        String query = "SELECT id, semester_name FROM semesters";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                semesters.add(resultSet.getString("id") + ": " + resultSet.getString("semester_name")); // Display "id: semester_name"
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error retrieving semesters: " + e.getMessage());
        }

        return semesters;
    }

    // Method to add module details to the database using sqlConnection
    private void addModule(String moduleName, String semesterId) {
        String query = "INSERT INTO modules (module_name, semester_id) VALUES (?, ?)";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, moduleName);
            statement.setString(2, semesterId);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                displaySuccessMessage("A new module was inserted successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error inserting module: " + e.getMessage());
        }
    }

    @FXML
    private void showAddClassForm() {
        clearFormContainer();
        VBox addClassForm = new VBox(10);
        TextField className = new TextField();
        className.setPromptText("Class Name");

        ComboBox<String> academicYearComboBox = new ComboBox<>();
        academicYearComboBox.getItems().addAll(getAcademicYears());
        academicYearComboBox.setPromptText("Select Academic Year");

        ComboBox<String> semesterComboBox = new ComboBox<>();
        semesterComboBox.getItems().addAll(getSemesters());
        semesterComboBox.setPromptText("Select Semester");

        Button addButton = new Button("Add Class");
        addButton.setOnAction(e -> {
            String selectedAcademicYear = academicYearComboBox.getValue();
            String selectedSemester = semesterComboBox.getValue();
            if (selectedAcademicYear != null && selectedSemester != null && !className.getText().isEmpty()) {
                addClass(className.getText(), selectedAcademicYear.split(": ")[1], selectedSemester.split(": ")[1]);
            } else {
                displayErrorMessage("Please fill out all fields.");
            }
        });

        addClassForm.getChildren().addAll(className, academicYearComboBox, semesterComboBox, addButton);
        formContainer.getChildren().add(addClassForm);
    }

    // Method to add class details to the database
    private void addClass(String className, String academicYear, String semester) {
        String query = "INSERT INTO classes (class_name, academic_year, semester) VALUES (?, ?, ?)";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, className);
            statement.setString(2, academicYear);
            statement.setString(3, semester);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                displaySuccessMessage("A new class was inserted successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error inserting class: " + e.getMessage());
        }
    }

    @FXML
    private void showAddStudentForm() {
        clearFormContainer();
        VBox addStudentForm = new VBox(10);

        TextField studentNumber = new TextField();
        studentNumber.setPromptText("Student Number");

        TextField studentName = new TextField();
        studentName.setPromptText("Student Name");

        ComboBox<String> classComboBox = new ComboBox<>();
        classComboBox.getItems().addAll(getClasses());
        classComboBox.setPromptText("Select Class");

        Button addButton = new Button("Add Student");
        addButton.setOnAction(e -> {
            String selectedClassId = classComboBox.getValue();
            if (selectedClassId != null && !studentNumber.getText().isEmpty() && !studentName.getText().isEmpty()) {
                addStudent(studentNumber.getText(), studentName.getText(), selectedClassId.split(": ")[0]); // Use ID for the class directly
            } else {
                displayErrorMessage("Please fill out all fields.");
            }
        });

        addStudentForm.getChildren().addAll(studentNumber, studentName, classComboBox, addButton);
        formContainer.getChildren().add(addStudentForm);
    }

    // Retrieve the list of classes from the database
    private List<String> getClasses() {
        List<String> classes = new ArrayList<>();
        String query = "SELECT id, class_name FROM classes"; // Assuming your class table is named "classes"

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                classes.add(resultSet.getString("id") + ": " + resultSet.getString("class_name")); // Display "id: class_name"
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error retrieving classes: " + e.getMessage());
        }

        return classes;
    }

    // Method to add student to the database
    private void addStudent(String studentNumber, String studentName, String classId) {
        String query = "INSERT INTO student (student_number, name, class) VALUES (?, ?, ?)";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, studentNumber);
            statement.setString(2, studentName);
            statement.setString(3, classId);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                displaySuccessMessage("A new student was inserted successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error inserting student: " + e.getMessage());
        }
    }

    @FXML
    private void showAssignLectureForm() {
        clearFormContainer();
        VBox assignLectureForm = new VBox(10);

        ComboBox<String> rolesComboBox = new ComboBox<>();
        rolesComboBox.getItems().addAll("Lecturer", "Assistant", "Guest Lecturer");
        rolesComboBox.setPromptText("Select Role");

        ComboBox<String> moduleComboBox = new ComboBox<>();
        moduleComboBox.getItems().addAll(getModules());
        moduleComboBox.setPromptText("Select Module");

        ComboBox<String> classComboBox = new ComboBox<>();
        classComboBox.getItems().addAll(getClasses());
        classComboBox.setPromptText("Select Class");

        ComboBox<String> lecturerComboBox = new ComboBox<>();
        lecturerComboBox.getItems().addAll(getLecturers());
        lecturerComboBox.setPromptText("Select Lecturer");

        Button assignButton = new Button("Assign Lecture");
        assignButton.setOnAction(e -> {
            String selectedRole = rolesComboBox.getValue();
            String selectedModule = moduleComboBox.getValue();
            String selectedClass = classComboBox.getValue();
            String selectedLecturer = lecturerComboBox.getValue();

            if (selectedRole != null && selectedModule != null && selectedClass != null && selectedLecturer != null) {
                String employeeNumber = selectedLecturer.split(": ")[0]; // Extract employee number
                String lectureName = selectedLecturer.split(": ")[1]; // Extract lecturer name
                assignLecture(selectedRole, selectedModule.split(": ")[0], selectedClass.split(": ")[0], lectureName, employeeNumber);
            } else {
                displayErrorMessage("Please fill out all fields.");
            }
        });

        assignLectureForm.getChildren().addAll(rolesComboBox, lecturerComboBox, moduleComboBox, classComboBox, assignButton);
        formContainer.getChildren().add(assignLectureForm);
    }

    // Retrieve the list of modules from the database
    private List<String> getModules() {
        List<String> modules = new ArrayList<>();
        String query = "SELECT id, module_name FROM modules";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                modules.add(resultSet.getString("id") + ": " + resultSet.getString("module_name")); // Display "id: module_name"
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error retrieving modules: " + e.getMessage());
        }
        return modules;
    }

    // Retrieve the list of lecturers from the database
    private List<String> getLecturers() {
        List<String> lecturers = new ArrayList<>();
        String query = "SELECT employee_number, name FROM lecturer";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String empNum = resultSet.getString("employee_number");
                String name = resultSet.getString("name");
                if (empNum != null && name != null && !name.isEmpty()) {
                    lecturers.add(empNum + ": " + name); // Display "employee_number: lecturer name"
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error retrieving lecturers: " + e.getMessage());
        }
        return lecturers;
    }

    // Assign lecture to a role, module, class, and lecturer
    private void assignLecture(String role, String moduleId, String classId, String lectureName, String employeeNumber) {
        String query = "INSERT INTO assign_lecture (roles, module, class, lectureName, employee_number) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, role);
            statement.setString(2, moduleId);
            statement.setString(3, classId);
            statement.setString(4, lectureName);
            statement.setString(5, employeeNumber);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                displaySuccessMessage("Lecture assigned successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error assigning lecture: " + e.getMessage());
        }
    }

    @FXML
    private void viewAll() {
        clearFormContainer();
        VBox viewAllContainer = new VBox(10);

        // Create buttons to view each table:
        Button viewAssignLectureBtn = new Button("View Assign Lecture");
        Button viewLecturersBtn = new Button("View Lecturers");
        Button viewModulesBtn = new Button("View Modules");
        Button viewSemestersBtn = new Button("View Semesters");

        viewAssignLectureBtn.setOnAction(e -> showAssignLectureTable(viewAllContainer));
        viewLecturersBtn.setOnAction(e -> showLecturersTable(viewAllContainer));
        viewModulesBtn.setOnAction(e -> showModulesTable(viewAllContainer));
        viewSemestersBtn.setOnAction(e -> showSemestersTable(viewAllContainer));

        // Adding buttons to the VBox
        viewAllContainer.getChildren().addAll(
                viewAssignLectureBtn, viewLecturersBtn, viewModulesBtn, viewSemestersBtn
        );

        formContainer.getChildren().add(viewAllContainer); // Add buttons to form container
    }

    // Method to show the Assign Lecture table
    private void showAssignLectureTable(VBox container) {
        TableView<AssignLecture> table = new TableView<>();
        TableColumn<AssignLecture, String> rolesCol = new TableColumn<>("Roles");
        rolesCol.setCellValueFactory(new PropertyValueFactory<>("roles"));

        TableColumn<AssignLecture, Integer> moduleCol = new TableColumn<>("Module");
        moduleCol.setCellValueFactory(new PropertyValueFactory<>("module"));

        TableColumn<AssignLecture, Integer> classCol = new TableColumn<>("Class");
        classCol.setCellValueFactory(new PropertyValueFactory<>("classId"));

        TableColumn<AssignLecture, String> lectureNameCol = new TableColumn<>("Lecture Name");
        lectureNameCol.setCellValueFactory(new PropertyValueFactory<>("lectureName"));

        TableColumn<AssignLecture, String> employeeNumberCol = new TableColumn<>("Employee Number");
        employeeNumberCol.setCellValueFactory(new PropertyValueFactory<>("employeeNumber"));

        table.getColumns().addAll(rolesCol, moduleCol, classCol, lectureNameCol, employeeNumberCol);

        List<AssignLecture> data = getAssignLectureData();
        ObservableList<AssignLecture> observableList = FXCollections.observableArrayList(data);
        table.setItems(observableList);

        container.getChildren().clear();
        container.getChildren().add(table); // Clear existing components and add table
    }

    // Fetching data from the assign_lecture table
    private List<AssignLecture> getAssignLectureData() {
        List<AssignLecture> assignLectures = new ArrayList<>();
        String query = "SELECT * FROM assign_lecture";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                assignLectures.add(new AssignLecture(
                        resultSet.getString("roles"),
                        resultSet.getInt("module"),
                        resultSet.getInt("class"),
                        resultSet.getString("lectureName"),
                        resultSet.getString("employee_number")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error fetching Assign Lectures: " + e.getMessage());
        }
        return assignLectures;
    }

    // Method to show the Lecturers table
    private void showLecturersTable(VBox container) {
        TableView<Lecturer> table = new TableView<>();
        TableColumn<Lecturer, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Lecturer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Lecturer, String> empNumCol = new TableColumn<>("Employee Number");
        empNumCol.setCellValueFactory(new PropertyValueFactory<>("employeeNumber"));

        table.getColumns().addAll(usernameCol, nameCol, empNumCol);

        List<Lecturer> data = getLecturerData();
        ObservableList<Lecturer> observableList = FXCollections.observableArrayList(data);
        table.setItems(observableList);

        container.getChildren().clear();
        container.getChildren().add(table);  // Clear existing components and add table
    }

    // Fetching data from the lecturer table
    private List<Lecturer> getLecturerData() {
        List<Lecturer> lecturers = new ArrayList<>();
        String query = "SELECT * FROM lecturer";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                lecturers.add(new Lecturer(
                        resultSet.getString("username"),
                        resultSet.getString("name"),
                        resultSet.getString("employee_number")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error fetching lecturers: " + e.getMessage());
        }
        return lecturers;
    }

    // Method to show the Modules table
    private void showModulesTable(VBox container) {
        TableView<Module> table = new TableView<>();
        TableColumn<Module, String> moduleNameCol = new TableColumn<>("Module Name");
        moduleNameCol.setCellValueFactory(new PropertyValueFactory<>("moduleName"));

        TableColumn<Module, Integer> semesterIdCol = new TableColumn<>("Semester ID");
        semesterIdCol.setCellValueFactory(new PropertyValueFactory<>("semesterId"));

        table.getColumns().addAll(moduleNameCol, semesterIdCol);

        List<Module> data = getModuleData();
        ObservableList<Module> observableList = FXCollections.observableArrayList(data);
        table.setItems(observableList);

        container.getChildren().clear();
        container.getChildren().add(table);  // Clear existing components and add table
    }

    // Fetching data from the modules table
    private List<Module> getModuleData() {
        List<Module> modules = new ArrayList<>();
        String query = "SELECT * FROM modules";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                modules.add(new Module(
                        resultSet.getString("module_name"),
                        resultSet.getInt("semester_id")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error fetching modules: " + e.getMessage());
        }
        return modules;
    }

    // Method to show the Semesters table
    private void showSemestersTable(VBox container) {
        TableView<Semester> table = new TableView<>();
        TableColumn<Semester, String> semesterNameCol = new TableColumn<>("Semester Name");
        semesterNameCol.setCellValueFactory(new PropertyValueFactory<>("semesterName"));

        TableColumn<Semester, Integer> academicYearIdCol = new TableColumn<>("Academic Year ID");
        academicYearIdCol.setCellValueFactory(new PropertyValueFactory<>("academicYearId"));

        table.getColumns().addAll(semesterNameCol, academicYearIdCol);

        List<Semester> data = getSemesterData();
        ObservableList<Semester> observableList = FXCollections.observableArrayList(data);
        table.setItems(observableList);

        container.getChildren().clear();
        container.getChildren().add(table); // Clear existing components and add table
    }

    // Fetching data from the semesters table
    private List<Semester> getSemesterData() {
        List<Semester> semesters = new ArrayList<>();
        String query = "SELECT * FROM semesters";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                semesters.add(new Semester(
                        resultSet.getString("semester_name"),
                        resultSet.getInt("academic_year_id")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayErrorMessage("Error fetching semesters: " + e.getMessage());
        }
        return semesters;
    }

    private void clearFormContainer() {
        formContainer.getChildren().clear(); // Clears the previous form when a new one is opened
    }

    public void Logout(ActionEvent actionEvent) {
        try {
            // Load the home.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home.fxml"));
            Parent root = loader.load();

            // Set the new scene to the current stage (window)
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            displayErrorMessage("Error during logout: " + e.getMessage());
        }
    }

    // Class to represent Assign Lecture
    public static class AssignLecture {
        private final String roles;
        private final int module;
        private final int classId;
        private final String lectureName;
        private final String employeeNumber;

        public AssignLecture(String roles, int module, int classId, String lectureName, String employeeNumber) {
            this.roles = roles;
            this.module = module;
            this.classId = classId;
            this.lectureName = lectureName;
            this.employeeNumber = employeeNumber;
        }

        public String getRoles() { return roles; }
        public int getModule() { return module; }
        public int getClassId() { return classId; }
        public String getLectureName() { return lectureName; }
        public String getEmployeeNumber() { return employeeNumber; }
    }

    // Class to represent Lecturer
    public static class Lecturer {
        private final String username;
        private final String name;
        private final String employeeNumber;

        public Lecturer(String username, String name, String employeeNumber) {
            this.username = username;
            this.name = name;
            this.employeeNumber = employeeNumber;
        }

        public String getUsername() { return username; }
        public String getName() { return name; }
        public String getEmployeeNumber() { return employeeNumber; }
    }

    // Class to represent Module
    public static class Module {
        private final String moduleName;
        private final int semesterId;

        public Module(String moduleName, int semesterId) {
            this.moduleName = moduleName;
            this.semesterId = semesterId;
        }

        public String getModuleName() { return moduleName; }
        public int getSemesterId() { return semesterId; }
    }

    // Class to represent Semester
    public static class Semester {
        private final String semesterName;
        private final int academicYearId;

        public Semester(String semesterName, int academicYearId) {
            this.semesterName = semesterName;
            this.academicYearId = academicYearId;
        }

        public String getSemesterName() { return semesterName; }
        public int getAcademicYearId() { return academicYearId; }
    }

    // Display error message in a pop-up dialog
    private void displayErrorMessage(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Display success message in a pop-up dialog
    private void displaySuccessMessage(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
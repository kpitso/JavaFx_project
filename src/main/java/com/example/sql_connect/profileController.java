package com.example.sql_connect;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class profileController {

    @FXML
    private Label userIdLabel; // Label for User ID
    @FXML
    private Label usernameLabel; // Label for Username
    @FXML
    private Label passwordLabel; // Label for Password
    @FXML
    private Label nameLabel; // Label for Name

    private sqlConnection database;

    public profileController() {
        database = new sqlConnection(); // Initialize the database connection
    }

    // Method to load the admin profile details
    public void loadProfile(String username) {
        String query = "SELECT * FROM admin";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            //statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()){
            userIdLabel.setText(resultSet.getString("id"));
            usernameLabel.setText(resultSet.getString("username"));
            passwordLabel.setText(resultSet.getString("password"));
            nameLabel.setText(resultSet.getString("name"));

        }

            /* if (resultSet.next()) {
                int userId = resultSet.getInt("id"); // Fetch the user id
                userIdLabel.setText(String.valueOf(userId)); // Convert integer to String
                usernameLabel.setText(resultSet.getString("username"));
                passwordLabel.setText(resultSet.getString("password"));
                nameLabel.setText(resultSet.getString("name")); // Display name in profile
            } else {
                userIdLabel.setText("not found");
                usernameLabel.setText("not found");
                passwordLabel.setText("not found");
                nameLabel.setText("not found");
            }*/

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error fetching admin details: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        // Implement the logic to go back to the admin dashboard
    }
}
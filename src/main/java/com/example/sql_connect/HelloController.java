package com.example.sql_connect;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class HelloController {

    @FXML
    private Label showName_Label;
    public void connectButton(ActionEvent event){
        sqlConnection connectNow = new sqlConnection();
        Connection connectDB = connectNow.getConnection();

        String Connectquery = "SELECT ClassName From class";

        try {
            Statement statement = connectDB.createStatement();
            ResultSet queryOutput;
            queryOutput = statement.executeQuery(Connectquery);

            while (queryOutput.next()){
                showName_Label.setText(queryOutput.getString("ClassName"));
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
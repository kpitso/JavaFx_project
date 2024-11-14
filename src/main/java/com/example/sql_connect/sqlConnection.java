package com.example.sql_connect;
import java.sql.Connection;
import java.sql.DriverManager;


public class sqlConnection {

    public Connection databaseLink;

    public Connection getConnection(){
        String databaseName= "university";
        String databaseUser = "root";
        String databasePassword = "12345";
         String url = "jdbc:mysql://localhost:3306/university";

        try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                databaseLink = DriverManager.getConnection(url, databaseUser, databasePassword);
        } catch (Exception e){
            e.printStackTrace();
        }

        return databaseLink;
    }
}

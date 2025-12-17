package org.example.calendario_app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private String url = "jdbc:mariadb://localhost:3306/calendario_app";
    private String user = "root";
    private String pass = "";

    public Connection getConn() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }
}
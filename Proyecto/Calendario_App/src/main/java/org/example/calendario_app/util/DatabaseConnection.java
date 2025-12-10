package org.example.calendario_app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String url = "jdbc:mariadb://localhost:3306/calendario_app";
    private static final String user = "root";
    private static final String pass = "";

    public Connection getConn() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }
}
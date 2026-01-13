package org.example.calendario_app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private String url = "jdbc:mariadb://vnlsjy.h.filess.io:3305/calendario_app_solutionso";
    private String user = "calendario_app_solutionso";
    private String pass = "e13dac639f08cb4fed987a36e7b8e30e95cad171";

    public Connection getConn() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }
}
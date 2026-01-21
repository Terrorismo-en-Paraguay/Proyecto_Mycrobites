package org.example.calendario_app.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SchemaCheck {
    public static void main(String[] args) {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MariaDB Driver not found!");
            e.printStackTrace();
            return;
        }

        DatabaseConnection db = new DatabaseConnection();
        try (Connection conn = db.getConn()) {
            if (conn != null) {
                // Try to get column details for 'grupos_usuarios'
                String query = "SHOW COLUMNS FROM grupos_usuarios LIKE 'rol'";
                try (PreparedStatement dbo = conn.prepareStatement(query);
                        ResultSet rs = dbo.executeQuery()) {
                    while (rs.next()) {
                        String field = rs.getString("Field");
                        String type = rs.getString("Type");
                        System.out.println("Field: " + field + ", Type: " + type);
                    }
                }
            } else {
                System.out.println("Failed to connect to DB");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

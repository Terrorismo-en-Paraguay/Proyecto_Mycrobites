package org.example.calendario_app.dao.impl;

import org.example.calendario_app.dao.UsuarioDAO;
import org.example.calendario_app.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAOImpl implements UsuarioDAO {
    DatabaseConnection databaseConnection;

    public UsuarioDAOImpl() {
        databaseConnection = new DatabaseConnection();
    }

    @Override
    public boolean iniciarSesion(String email, String password) {
        String query = "SELECT * FROM usuarios WHERE correo = ? AND password_hash = ?";
        try (Connection conn = databaseConnection.getConn();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

package org.example.calendario_app.dao.impl;

import org.example.calendario_app.dao.UsuarioDAO;
import org.example.calendario_app.model.Usuario;
import org.example.calendario_app.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl {
    DatabaseConnection databaseConnection;

    public UsuarioDAOImpl() {
        databaseConnection = new DatabaseConnection();
    }


    public List<String> iniciarSesion(String email) {
        List<String> Login = new ArrayList<String>();
        String query = "SELECT * FROM usuarios WHERE correo = '"+email+"'";
        try (Connection conn = databaseConnection.getConn(); Statement stmt = databaseConnection.getConn().createStatement();ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Login.add(rs.getString("correo"));
                Login.add(rs.getString("password_hash"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Login;
    }
}

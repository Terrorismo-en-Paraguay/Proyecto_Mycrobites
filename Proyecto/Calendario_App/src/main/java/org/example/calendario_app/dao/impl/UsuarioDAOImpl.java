package org.example.calendario_app.dao.impl;

import org.example.calendario_app.dao.UsuarioDAO;
import org.example.calendario_app.model.Usuario;
import org.example.calendario_app.util.DatabaseConnection;

import java.sql.*;

public class UsuarioDAOImpl {
    DatabaseConnection databaseConnection;

    public UsuarioDAOImpl() {
        databaseConnection = new DatabaseConnection();
    }

    public Usuario iniciarSesion(String email) {
        Usuario usuario = null;
        String query = "SELECT * FROM usuarios WHERE correo = '" + email + "'";
        try (Connection conn = databaseConnection.getConn();
                Statement stmt = databaseConnection.getConn().createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                usuario = new Usuario(
                        rs.getInt("id_cliente"),
                        rs.getString("correo"),
                        rs.getString("password_hash"),
                        "USER"
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuario;
    }

    public boolean registrar(Usuario usuario) {
        String query = "INSERT INTO usuarios (id_cliente, correo, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, usuario.getId_cliente());
            pstmt.setString(2, usuario.getCorreo());
            pstmt.setString(3, usuario.getPassword_hash());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

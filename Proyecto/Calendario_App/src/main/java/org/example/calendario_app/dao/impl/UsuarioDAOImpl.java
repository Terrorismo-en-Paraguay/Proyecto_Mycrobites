package org.example.calendario_app.dao.impl;

import org.example.calendario_app.model.Usuario;
import org.example.calendario_app.util.DatabaseConnection;

import java.sql.*;
import java.util.List;

public class UsuarioDAOImpl {
    DatabaseConnection databaseConnection;

    public UsuarioDAOImpl() {
        databaseConnection = new DatabaseConnection();
    }

    public Usuario iniciarSesion(String email) {
        Usuario usuario = null;
        String query = "SELECT * FROM usuarios WHERE correo = ?";
        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getInt("id_cliente"),
                            rs.getString("correo"),
                            rs.getString("password_hash"),
                            "USER");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuario;
    }

    public int registrar(Usuario usuario) {
        String query = "INSERT INTO usuarios (id_cliente, correo, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, usuario.getId_cliente());
            pstmt.setString(2, usuario.getCorreo());
            pstmt.setString(3, usuario.getPassword_hash());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Usuario findByEmail(String email) {
        Usuario usuario = null;
        String query = "SELECT * FROM usuarios WHERE correo = ?";
        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getInt("id_cliente"),
                            rs.getString("correo"),
                            rs.getString("password_hash"),
                            "USER");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuario;
    }

    public List<Usuario> findAll() {
        java.util.List<Usuario> usuarios = new java.util.ArrayList<>();
        String query = "SELECT * FROM usuarios";
        try (Connection conn = databaseConnection.getConn();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                usuarios.add(new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getInt("id_cliente"),
                        rs.getString("correo"),
                        rs.getString("password_hash"),
                        "USER"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarios;
    }

    public boolean updatePassword(int userId, String newPasswordHash) {
        String query = "UPDATE usuarios SET password_hash = ? WHERE id_usuario = ?";
        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newPasswordHash);
            pstmt.setInt(2, userId);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

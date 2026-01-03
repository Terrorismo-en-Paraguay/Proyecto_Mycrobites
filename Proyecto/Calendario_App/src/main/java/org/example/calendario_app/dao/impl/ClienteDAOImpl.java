package org.example.calendario_app.dao.impl;

import org.example.calendario_app.model.Cliente;
import org.example.calendario_app.util.DatabaseConnection;

import java.sql.*;

public class ClienteDAOImpl {
    DatabaseConnection databaseConnection;

    public ClienteDAOImpl() {
        databaseConnection = new DatabaseConnection();
    }

    public int registrar(Cliente cliente) {
        String query = "INSERT INTO clientes (nombre, apellidos) VALUES (?, ?)";
        int idGenerado = -1;

        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getApellidos());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idGenerado = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idGenerado;
    }

    public Cliente obtenerPorId(int id) {
        String query = "SELECT * FROM clientes WHERE id_cliente = " + id;
        Cliente cliente = null;
        try (Connection conn = databaseConnection.getConn();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                cliente = new Cliente(
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        null // Fecha registro not strictly needed for UI now, or
                             // java.sql.Date.valueOf(rs.getDate("fecha_registro").toLocalDate()) if needed
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cliente;
    }
}

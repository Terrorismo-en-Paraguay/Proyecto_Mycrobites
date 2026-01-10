package org.example.calendario_app.dao.impl;

import org.example.calendario_app.dao.EtiquetaDAO;
import org.example.calendario_app.model.Etiqueta;
import org.example.calendario_app.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EtiquetaDAOImpl implements EtiquetaDAO {
    private final DatabaseConnection databaseConnection;

    public EtiquetaDAOImpl() {
        this.databaseConnection = new DatabaseConnection();
    }

    @Override
    public List<Etiqueta> findAllByUsuarioId(int idUsuario) {
        List<Etiqueta> etiquetas = new ArrayList<>();
        // Join with etiquetas_usuarios to get labels for this user
        String query = "SELECT e.* FROM etiquetas e " +
                "JOIN etiquetas_usuarios eu ON e.id_etiqueta = eu.id_etiqueta " +
                "WHERE eu.id_usuario = ?";

        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    etiquetas.add(new Etiqueta(
                            rs.getInt("id_etiqueta"),
                            rs.getString("nombre"),
                            rs.getString("color")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return etiquetas;
    }

    @Override
    public int save(Etiqueta etiqueta, int idUsuario, Integer idGrupo) {
        String insertEtiqueta = "INSERT INTO etiquetas (nombre, color) VALUES (?, ?)";
        String linkUser = "INSERT INTO etiquetas_usuarios (id_etiqueta, id_usuario) VALUES (?, ?)";
        String linkGroup = "INSERT INTO etiquetas_grupos (id_etiqueta, id_grupo) VALUES (?, ?)";

        int idGenerado = -1;
        Connection conn = null;

        try {
            conn = databaseConnection.getConn();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert Etiqueta
            try (PreparedStatement pstmt1 = conn.prepareStatement(insertEtiqueta, Statement.RETURN_GENERATED_KEYS)) {
                pstmt1.setString(1, etiqueta.getNombre());
                pstmt1.setString(2, etiqueta.getColor());
                pstmt1.executeUpdate();

                try (ResultSet generatedKeys = pstmt1.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idGenerado = generatedKeys.getInt(1);
                        etiqueta.setId(idGenerado);
                    }
                }
            }

            // 2. Link to User or Group
            if (idGenerado != -1) {
                // If linked to group, we might still want to link to user or not?
                // Requirement says associate to group.
                // Assuming unrelated logic: User can have personal labels, Group can have
                // labels.

                if (idGrupo != null) {
                    try (PreparedStatement pstmt3 = conn.prepareStatement(linkGroup)) {
                        pstmt3.setInt(1, idGenerado);
                        pstmt3.setInt(2, idGrupo);
                        pstmt3.executeUpdate();
                    }
                } else {
                    // Only link to user if NOT a group label? Or always link to user?
                    // Typically 'Personal' vs 'Group' labels.
                    // If idGrupo is null, link to user.
                    try (PreparedStatement pstmt2 = conn.prepareStatement(linkUser)) {
                        pstmt2.setInt(1, idGenerado);
                        pstmt2.setInt(2, idUsuario);
                        pstmt2.executeUpdate();
                    }
                }
            }

            conn.commit(); // Commit transaction

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return idGenerado;
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM etiquetas WHERE id_etiqueta = ?";
        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

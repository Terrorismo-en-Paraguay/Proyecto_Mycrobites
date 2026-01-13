package org.example.calendario_app.dao.impl;

import org.example.calendario_app.model.Grupo;
import org.example.calendario_app.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GrupoDAOImpl {
    DatabaseConnection databaseConnection;

    public GrupoDAOImpl() {
        databaseConnection = new DatabaseConnection();
    }

    public int create(Grupo grupo, int idCreator) {
        String insertGrupoQuery = "INSERT INTO grupos (nombre, descripcion) VALUES (?, ?)";
        String insertMemberQuery = "INSERT INTO grupos_usuarios (id_grupo, id_usuario, rol) VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmtGroup = null;
        PreparedStatement pstmtMember = null;
        ResultSet generatedKeys = null;

        try {
            conn = databaseConnection.getConn();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Insert Group
            pstmtGroup = conn.prepareStatement(insertGrupoQuery, Statement.RETURN_GENERATED_KEYS);
            pstmtGroup.setString(1, grupo.getNombre());
            pstmtGroup.setString(2, grupo.getDescripcion());

            int affectedRows = pstmtGroup.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return -1;
            }

            // 2. Get Group ID
            int groupId = -1;
            generatedKeys = pstmtGroup.getGeneratedKeys();
            if (generatedKeys.next()) {
                groupId = generatedKeys.getInt(1);
            } else {
                conn.rollback();
                return -1;
            }
            grupo.setId_grupo(groupId);

            // 3. Insert Creator as ADMIN
            pstmtMember = conn.prepareStatement(insertMemberQuery);
            pstmtMember.setInt(1, groupId);
            pstmtMember.setInt(2, idCreator);
            pstmtMember.setString(3, "admin");

            pstmtMember.executeUpdate();

            conn.commit(); // Commit Transaction
            return groupId;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return -1;
        } finally {
            try {
                if (generatedKeys != null)
                    generatedKeys.close();
                if (pstmtGroup != null)
                    pstmtGroup.close();
                if (pstmtMember != null)
                    pstmtMember.close();
                if (conn != null)
                    conn.setAutoCommit(true); // Reset auto-commit
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Do NOT close 'conn' if it's managed by DatabaseConnection and reused,
            // but typical DAO patterns close connection or return it to pool.
            // Assuming DatabaseConnection.getConn() returns a shared connection that
            // shouldn't be closed here,
            // OR a new connection. Standard practice with provided snippets suggests we
            // just close the Statements.
            // If DatabaseConnection creates a new connection each time, we should close it.
            // Assuming we must close specific resources.
        }
        // Assuming we must close specific resources.
    }

    public List<Grupo> findAllByUserId(int userId) {
        List<Grupo> grupos = new ArrayList<>();
        String query = "SELECT g.* FROM grupos g " +
                "JOIN grupos_usuarios gu ON g.id_grupo = gu.id_grupo " +
                "WHERE gu.id_usuario = ?";

        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Grupo grupo = new Grupo(
                            rs.getInt("id_grupo"),
                            rs.getString("nombre"),
                            rs.getString("descripcion"));
                    grupos.add(grupo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grupos;
    }

    public void addMember(int groupId, int userId, String role) {
        String insertMemberQuery = "INSERT INTO grupos_usuarios (id_grupo, id_usuario, rol) VALUES (?, ?, ?)";
        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(insertMemberQuery)) {
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

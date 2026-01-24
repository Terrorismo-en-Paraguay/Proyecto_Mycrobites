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

        try (Connection conn = databaseConnection.getConn()) {
            conn.setAutoCommit(false);

            int groupId = -1;
            try (PreparedStatement pstmtGroup = conn.prepareStatement(insertGrupoQuery,
                    Statement.RETURN_GENERATED_KEYS)) {
                pstmtGroup.setString(1, grupo.getNombre());
                pstmtGroup.setString(2, grupo.getDescripcion());
                pstmtGroup.executeUpdate();

                try (ResultSet generatedKeys = pstmtGroup.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        groupId = generatedKeys.getInt(1);
                    } else {
                        conn.rollback();
                        return -1;
                    }
                }
            }

            grupo.setId_grupo(groupId);

            try (PreparedStatement pstmtMember = conn.prepareStatement(insertMemberQuery)) {
                pstmtMember.setInt(1, groupId);
                pstmtMember.setInt(2, idCreator);
                pstmtMember.setString(3, "admin");
                pstmtMember.executeUpdate();
            }

            conn.commit();
            return groupId;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
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

    public boolean addMember(int groupId, int userId, String role) {
        String insertMemberQuery = "INSERT INTO grupos_usuarios (id_grupo, id_usuario, rol) VALUES (?, ?, ?)";
        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(insertMemberQuery)) {
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, role);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Integer> findMembersByGroupId(int groupId) {
        List<Integer> memberIds = new ArrayList<>();
        String query = "SELECT id_usuario FROM grupos_usuarios WHERE id_grupo = ?";
        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    memberIds.add(rs.getInt("id_usuario"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return memberIds;
    }
}

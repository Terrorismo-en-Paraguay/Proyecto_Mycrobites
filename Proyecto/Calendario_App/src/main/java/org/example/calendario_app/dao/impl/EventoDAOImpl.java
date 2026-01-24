package org.example.calendario_app.dao.impl;

import org.example.calendario_app.dao.EventoDAO;
import org.example.calendario_app.model.Evento;
import org.example.calendario_app.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventoDAOImpl implements EventoDAO {
    private final DatabaseConnection databaseConnection;

    public EventoDAOImpl() {
        this.databaseConnection = new DatabaseConnection();
    }

    @Override
    public List<Evento> findAllByUsuarioId(int idUsuario) {
        List<Evento> eventos = new ArrayList<>();
        // Query to get:
        // 1. Events created by the user
        // 2. Events that have a label which is shared with the user (via
        // etiquetas_usuarios)
        // 3. Events that have a label which is shared with a group the user is in (via
        // etiquetas_grupos -> grupos_usuarios)

        // Query to get:
        // 1. Events created by the user
        // 2. Events that have a label which is shared with the user (via
        // etiquetas_usuarios)

        String query = """
                    SELECT DISTINCT ev.*
                    FROM eventos ev
                    LEFT JOIN etiquetas e ON ev.id_etiqueta = e.id_etiqueta
                    LEFT JOIN etiquetas_usuarios eu ON e.id_etiqueta = eu.id_etiqueta
                    LEFT JOIN eventos_usuarios evu ON ev.id_evento = evu.id_evento
                    WHERE ev.id_creador = ?
                       OR eu.id_usuario = ?
                       OR evu.id_usuario = ?
                """;

        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idUsuario);
            pstmt.setInt(2, idUsuario);
            pstmt.setInt(3, idUsuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp startTs = rs.getTimestamp("fecha_inicio");
                    Timestamp endTs = rs.getTimestamp("fecha_fin");

                    eventos.add(new Evento(
                            rs.getInt("id_evento"),
                            rs.getString("titulo"),
                            rs.getString("descripcion"),
                            startTs != null ? startTs.toLocalDateTime() : null,
                            endTs != null ? endTs.toLocalDateTime() : null,
                            rs.getString("ubicacion"),
                            rs.getInt("id_creador"),
                            (Integer) rs.getObject("id_etiqueta")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eventos;
    }

    @Override
    public int save(Evento evento) {
        String query = "INSERT INTO eventos (titulo, descripcion, fecha_inicio, fecha_fin, ubicacion, id_creador, id_etiqueta) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int idGenerado = -1;

        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            System.out.println("Saving Event: Title=" + evento.getTitulo() + ", LabelID=" + evento.getId_etiqueta());

            pstmt.setString(1, evento.getTitulo());
            pstmt.setString(2, evento.getDescripcion());
            pstmt.setTimestamp(3, Timestamp.valueOf(evento.getFecha_inicio()));
            pstmt.setTimestamp(4, evento.getFecha_fin() != null ? Timestamp.valueOf(evento.getFecha_fin()) : null);
            pstmt.setString(5, evento.getUbicacion());
            pstmt.setInt(6, evento.getId_creador());
            if (evento.getId_etiqueta() != null) {
                pstmt.setInt(7, evento.getId_etiqueta());
            } else {
                pstmt.setNull(7, java.sql.Types.INTEGER);
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idGenerado = generatedKeys.getInt(1);
                        evento.setId(idGenerado);

                        // Auto-sharing logic (New)
                        if (evento.getId_etiqueta() != null) {
                            shareWithGroupMembers(conn, idGenerado, evento.getId_etiqueta(), evento.getId_creador());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idGenerado;
    }

    private void shareWithGroupMembers(Connection conn, int eventId, int labelId, int creatorId) throws SQLException {
        // 1. Check if label is linked to a group
        String checkGroupQuery = "SELECT id_grupo FROM etiquetas WHERE id_etiqueta = ?";
        Integer groupId = null;
        try (PreparedStatement pstmt = conn.prepareStatement(checkGroupQuery)) {
            pstmt.setInt(1, labelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    groupId = (Integer) rs.getObject("id_grupo");
                }
            }
        }

        // 2. If group exists, invite all members (except creator)
        if (groupId != null) {
            String membersQuery = "SELECT id_usuario FROM grupos_usuarios WHERE id_grupo = ? AND id_usuario != ?";
            String inviteQuery = "INSERT INTO eventos_usuarios (id_evento, id_usuario, estado, notificado) VALUES (?, ?, 'pendiente', 0) ON DUPLICATE KEY UPDATE id_usuario=id_usuario";

            try (PreparedStatement pstmtMembers = conn.prepareStatement(membersQuery);
                    PreparedStatement pstmtInvite = conn.prepareStatement(inviteQuery)) {

                pstmtMembers.setInt(1, groupId);
                pstmtMembers.setInt(2, creatorId);

                try (ResultSet rs = pstmtMembers.executeQuery()) {
                    while (rs.next()) {
                        int userId = rs.getInt("id_usuario");
                        pstmtInvite.setInt(1, eventId);
                        pstmtInvite.setInt(2, userId);
                        pstmtInvite.executeUpdate();
                    }
                }
            }
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM eventos WHERE id_evento = ?";
        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

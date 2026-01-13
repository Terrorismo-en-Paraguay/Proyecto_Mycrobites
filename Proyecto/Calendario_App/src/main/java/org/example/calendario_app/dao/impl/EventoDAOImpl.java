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

        String query = """
                    SELECT DISTINCT ev.*
                    FROM events ev
                    LEFT JOIN etiquetas e ON ev.id_etiqueta = e.id_etiqueta
                    LEFT JOIN etiquetas_usuarios eu ON e.id_etiqueta = eu.id_etiqueta
                    LEFT JOIN etiquetas_grupos eg ON e.id_etiqueta = eg.id_etiqueta
                    LEFT JOIN grupos_usuarios gu ON eg.id_grupo = gu.id_grupo
                    WHERE ev.id_creador = ?
                       OR eu.id_usuario = ?
                       OR gu.id_usuario = ?
                """;

        // Note: 'events' table name in original code was 'eventos'?
        // Checking previous view_file of EventoDAOImpl...
        // Original query was: "SELECT * FROM eventos WHERE id_creador = ?";
        // So table name is 'eventos'.

        String queryCorrected = """
                    SELECT DISTINCT ev.*
                    FROM eventos ev
                    LEFT JOIN etiquetas e ON ev.id_etiqueta = e.id_etiqueta
                    LEFT JOIN etiquetas_usuarios eu ON e.id_etiqueta = eu.id_etiqueta
                    LEFT JOIN etiquetas_grupos eg ON e.id_etiqueta = eg.id_etiqueta
                    LEFT JOIN grupos_usuarios gu ON eg.id_grupo = gu.id_grupo
                    WHERE ev.id_creador = ?
                       OR eu.id_usuario = ?
                       OR gu.id_usuario = ?
                """;

        try (Connection conn = databaseConnection.getConn();
                PreparedStatement pstmt = conn.prepareStatement(queryCorrected)) {

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
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idGenerado;
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

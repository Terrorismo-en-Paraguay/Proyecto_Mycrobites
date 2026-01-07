package org.example.calendario_app.dao.impl;

import org.example.calendario_app.dao.FestivoDAO;
import org.example.calendario_app.model.Festivo;
import org.example.calendario_app.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FestivoDAOImpl implements FestivoDAO {
    private final DatabaseConnection databaseConnection;

    public FestivoDAOImpl() {
        this.databaseConnection = new DatabaseConnection();
    }

    @Override
    public List<Festivo> findAll() {
        List<Festivo> festivos = new ArrayList<>();
        String query = "SELECT * FROM festivos";

        try (Connection conn = databaseConnection.getConn();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                festivos.add(new Festivo(
                        rs.getInt("id_festivo"),
                        rs.getString("nombre"),
                        rs.getInt("dia"),
                        rs.getInt("mes")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return festivos;
    }
}

package org.example.calendario_app.dao.impl;

import org.example.calendario_app.dao.UsuarioDAO;
import org.example.calendario_app.util.DatabaseConnection;

public class UsuarioDAOImpl implements UsuarioDAO {
    DatabaseConnection databaseConnection;
    public UsuarioDAOImpl() {
        databaseConnection = new DatabaseConnection();
    }
    @Override
    public void iniciarSesion() {

    }
}

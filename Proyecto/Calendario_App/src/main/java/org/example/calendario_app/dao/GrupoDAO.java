package org.example.calendario_app.dao;

import org.example.calendario_app.dao.impl.GrupoDAOImpl;
import org.example.calendario_app.model.Grupo;

import java.util.List;

public class GrupoDAO {
    GrupoDAOImpl grupoDAO;

    public GrupoDAO(GrupoDAOImpl grupoDAO) {
        this.grupoDAO = grupoDAO;
    }

    public int create(Grupo grupo, int idCreator) {
        return grupoDAO.create(grupo, idCreator);
    }

    public List<Grupo> findAllByUserId(int userId) {
        return grupoDAO.findAllByUserId(userId);
    }
}

package org.example.calendario_app.dao;

import org.example.calendario_app.model.Evento;
import java.util.List;

public interface EventoDAO {
    List<Evento> findAllByUsuarioId(int idUsuario);

    int save(Evento evento);

    void delete(int id);
}

package org.example.calendario_app.dao;

import org.example.calendario_app.model.Etiqueta;
import java.util.List;

public interface EtiquetaDAO {
    List<Etiqueta> findAllByUsuarioId(int idUsuario);

    int save(Etiqueta etiqueta, int idUsuario, Integer idGrupo);

    void updateGroupId(int labelId, Integer groupId);

    void delete(int id);
}

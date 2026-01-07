package org.example.calendario_app.dao;

import org.example.calendario_app.model.Festivo;
import java.util.List;

public interface FestivoDAO {
    List<Festivo> findAll();
}

package org.example.calendario_app.dao;

import org.example.calendario_app.dao.impl.ClienteDAOImpl;
import org.example.calendario_app.model.Cliente;

public class ClienteDAO {
    ClienteDAOImpl clienteDAO;

    public ClienteDAO(ClienteDAOImpl clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    public int registrar(Cliente cliente) {
        return clienteDAO.registrar(cliente);
    }

    public Cliente obtenerPorId(int id) {
        return clienteDAO.obtenerPorId(id);
    }
}

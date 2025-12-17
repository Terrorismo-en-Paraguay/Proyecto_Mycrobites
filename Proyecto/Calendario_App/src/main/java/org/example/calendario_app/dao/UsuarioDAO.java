package org.example.calendario_app.dao;

import org.example.calendario_app.dao.impl.UsuarioDAOImpl;
import org.example.calendario_app.model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class UsuarioDAO {
    UsuarioDAOImpl usuarioDAO;

    public UsuarioDAO(UsuarioDAOImpl usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public boolean iniciarSesion(String email, String password) {
        boolean login = false;
        List<String> datos_login = usuarioDAO.iniciarSesion(email);
        if (!datos_login.isEmpty()) {
            login = email.equals(datos_login.get(0)) && password.equals(datos_login.get(1));
        } else {
            System.out.println("Usuario no encontrado");
        }
        return login;
    }

    public boolean registrar(Usuario usuario) {
        return usuarioDAO.registrar(usuario);
    }
}

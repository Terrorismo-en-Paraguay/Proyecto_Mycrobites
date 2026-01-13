package org.example.calendario_app.dao;

import org.example.calendario_app.dao.impl.UsuarioDAOImpl;
import org.example.calendario_app.model.Usuario;

public class UsuarioDAO {
    UsuarioDAOImpl usuarioDAO;

    public UsuarioDAO(UsuarioDAOImpl usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario iniciarSesion(String email, String password) {
        Usuario usuario = usuarioDAO.iniciarSesion(email);
        if (usuario != null && usuario.getPassword_hash().equals(password)) {
            return usuario;
        } else {
            System.out.println("Usuario no encontrado o contraseña incorrecta");
            return null;
        }
    }

    public int registrar(Usuario usuario) {
        return usuarioDAO.registrar(usuario);
    }

    public Usuario findByEmail(String email) {
        return usuarioDAO.findByEmail(email);
    }
}

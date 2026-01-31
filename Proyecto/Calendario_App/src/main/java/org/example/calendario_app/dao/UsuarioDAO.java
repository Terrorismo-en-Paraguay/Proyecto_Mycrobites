package org.example.calendario_app.dao;

import org.example.calendario_app.dao.impl.UsuarioDAOImpl;
import org.example.calendario_app.model.Usuario;

import java.util.List;

public class UsuarioDAO {
    UsuarioDAOImpl usuarioDAO;

    public UsuarioDAO(UsuarioDAOImpl usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario iniciarSesion(String email, String password) {
        Usuario usuario = usuarioDAO.iniciarSesion(email);
        if (usuario != null) {
            String hashedInput = org.example.calendario_app.util.PasswordUtil.hash(password);
            if (usuario.getPassword_hash().equals(hashedInput)) {
                return usuario;
            }
        }
        System.out.println("Usuario no encontrado o contraseña incorrecta");
        return null;
    }

    public int registrar(Usuario usuario) {
        // Hash the password before saving
        String hashedPassword = org.example.calendario_app.util.PasswordUtil.hash(usuario.getPassword_hash());
        usuario.setPassword_hash(hashedPassword);
        return usuarioDAO.registrar(usuario);
    }

    public Usuario findByEmail(String email) {
        return usuarioDAO.findByEmail(email);
    }

    public List<Usuario> findAll() {
        return usuarioDAO.findAll();
    }
}

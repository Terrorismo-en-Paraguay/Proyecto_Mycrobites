package org.example.calendario_app.util;

import org.example.calendario_app.model.Cliente;
import org.example.calendario_app.model.Usuario;

public class Session {
    private static Session instance;
    private Usuario usuario;
    private Cliente cliente;

    private Session() {
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void clear() {
        usuario = null;
        cliente = null;
    }
}

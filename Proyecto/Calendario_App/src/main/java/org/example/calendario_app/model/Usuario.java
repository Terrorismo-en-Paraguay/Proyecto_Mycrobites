package org.example.calendario_app.model;

public class Usuario {
    int id_usuario;
    int id_cliente;
    String correo;
    String password_hash;
    String rol;

    @Override
    public String toString() {
        return "Usuario{" +
                "id_usuario=" + id_usuario +
                ", id_cliente=" + id_cliente +
                ", correo='" + correo + '\'' +
                ", password_hash='" + password_hash + '\'' +
                ", rol='" + rol + '\'' +
                '}';
    }
}

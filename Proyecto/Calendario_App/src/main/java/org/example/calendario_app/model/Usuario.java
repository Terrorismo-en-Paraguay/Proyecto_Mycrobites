package org.example.calendario_app.model;

public class Usuario {
    private int id_usuario;
    private int id_cliente;
    private String correo;
    private String password_hash;
    private String rol;

    public Usuario() {
    }

    public Usuario(int id_cliente, String correo, String password_hash, String rol) {
        this.id_cliente = id_cliente;
        this.correo = correo;
        this.password_hash = password_hash;
        this.rol = rol;
    }

    public Usuario(int id_usuario, int id_cliente, String correo, String password_hash, String rol) {
        this.id_usuario = id_usuario;
        this.id_cliente = id_cliente;
        this.correo = correo;
        this.password_hash = password_hash;
        this.rol = rol;
    }

    public int getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public int getId_cliente() {
        return id_cliente;
    }

    public void setId_cliente(int id_cliente) {
        this.id_cliente = id_cliente;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public int getId() {
        return id_usuario;
    }

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

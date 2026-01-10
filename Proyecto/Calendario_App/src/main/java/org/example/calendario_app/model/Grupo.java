package org.example.calendario_app.model;

public class Grupo {
    private int id_grupo;
    private String nombre;
    private String descripcion;
    // timestamp fields skipped for basic model unless needed

    public Grupo(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Grupo(int id_grupo, String nombre, String descripcion) {
        this.id_grupo = id_grupo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getId_grupo() {
        return id_grupo;
    }

    public void setId_grupo(int id_grupo) {
        this.id_grupo = id_grupo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

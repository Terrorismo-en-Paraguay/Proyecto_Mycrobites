package org.example.calendario_app.model;

public class Etiqueta {
    private int id_etiqueta;
    private String nombre;
    private String color;

    public Etiqueta() {
    }

    public Etiqueta(int id_etiqueta, String nombre, String color) {
        this.id_etiqueta = id_etiqueta;
        this.nombre = nombre;
        this.color = color;
    }

    public Etiqueta(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }

    public int getId() {
        return id_etiqueta;
    }

    public void setId(int id_etiqueta) {
        this.id_etiqueta = id_etiqueta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}

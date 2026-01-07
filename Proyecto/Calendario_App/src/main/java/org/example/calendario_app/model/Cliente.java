package org.example.calendario_app.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Cliente {
    private int id_cliente;
    private String nombre;
    private String apellidos;
    private LocalDate fecha_creacion;
    private LocalDate fecha_modificacion;

    public Cliente(String nombre, String apellidos, LocalDate fecha_creacion) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fecha_creacion = fecha_creacion;
    }

    public Cliente(int id_cliente, String nombre, String apellidos, LocalDate fecha_creacion,
            LocalDate fecha_modificacion) {
        this.id_cliente = id_cliente;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fecha_creacion = fecha_creacion;
        this.fecha_modificacion = fecha_modificacion;
    }

    public int getId() {
        return id_cliente;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id_cliente=" + id_cliente +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", fecha_creacion=" + fecha_creacion +
                ", fecha_modificacion=" + fecha_modificacion +
                '}';
    }
}

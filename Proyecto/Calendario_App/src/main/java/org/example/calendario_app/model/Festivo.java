package org.example.calendario_app.model;

import java.time.LocalDate;

public class Festivo {
    private int id_festivo;
    private String nombre;
    private int dia;
    private int mes;

    public Festivo(int id_festivo, String nombre, int dia, int mes) {
        this.id_festivo = id_festivo;
        this.nombre = nombre;
        this.dia = dia;
        this.mes = mes;
    }

    public int getId() {
        return id_festivo;
    }

    public void setId(int id_festivo) {
        this.id_festivo = id_festivo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }
}

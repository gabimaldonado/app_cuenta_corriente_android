package com.example.appcuentacorriente;
public class Cliente {
    public Integer id;
    public String nombre;
    public String telefono;
    public String dni;
    public String direccion;
    public String ciudad;
    public String notas;
    public Double latitud;
    public Double longitud;
    public Integer activo;

    public Cliente() {
    }

    public Cliente(String nombre, String telefono, String dni, String direccion,
                   String ciudad, String notas) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.dni = (dni == null || dni.isEmpty()) ? null : dni;
        this.direccion = (direccion == null || direccion. isEmpty()) ? null : direccion;
        this.ciudad = (ciudad == null || ciudad.isEmpty()) ? null : ciudad;
        this.notas = (notas == null || notas.isEmpty()) ? null : notas;
    }

    @Override
    public String toString() {
        String dniTexto = (dni != null && !dni.isEmpty()) ? "DNI: " + dni : "Sin DNI";
        String ciudadTexto = (ciudad != null && !ciudad.isEmpty()) ? " . " + ciudad : "";
        return nombre + " (" + dniTexto + ciudadTexto + ") \nTel: " + telefono;
    }
}

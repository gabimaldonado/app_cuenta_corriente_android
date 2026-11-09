// java/com.example.appcuentacorriente/Entrega.java — una entrega tal como viaja en el JSON
package com.example.appcuentacorriente;

import java.util.List;

public class Entrega {

    // Los nombres de los atributos son las claves del JSON de entregas.php
    private int id;
    private int cliente_id;
    private String fecha;                      // texto: "2026-09-28 17:00:00"
    private String estado;                     // "abierta" o "cerrada"
    private List<EntregaItem> entrega_items;   // puede venir null (el POST no la devuelve)

    // Gson usa este constructor vacío al convertir el JSON en objetos
    public Entrega() {
    }

    public int getId() {
        return id;
    }

    public int getCliente_id() {
        return cliente_id;
    }

    public String getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public List<EntregaItem> getEntrega_items() {
        return entrega_items;
    }

    // Lo que muestra cada fila de una lista de entregas
    @Override
    public String toString() {
        return "Entrega " + id + " · " + fecha;
    }
}

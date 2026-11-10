// java/com.example.appcuentacorriente/EntregaItem.java — una prenda de una entrega
package com.example.appcuentacorriente;

public class EntregaItem {

    // Los nombres de los atributos son las claves del JSON de entregas.php
    private int id;                   // id del ítem (el que viaja en confirmar_entrega.php)
    private int entrega_id;
    private int producto_id;          // id de la prenda en el catálogo
    private double precio_unitario;   // el precio que tenía ese día
    private String estado;            // "en_prueba", "comprada" o "devuelta"

    // Gson usa este constructor vacío al convertir el JSON en objetos
    public EntregaItem() {
    }

    public int getId() {
        return id;
    }

    public int getEntrega_id() {
        return entrega_id;
    }

    public int getProducto_id() {
        return producto_id;
    }

    public double getPrecio_unitario() {
        return precio_unitario;
    }

    public String getEstado() {
        return estado;
    }
}

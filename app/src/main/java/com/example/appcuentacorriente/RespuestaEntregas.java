package com.example.appcuentacorriente;

import java.util.List;

public class RespuestaEntregas {
    int id;
    int cliente_id;
    String fecha;
    String estado;
    List<ItemEntrega> entrega_items;
}

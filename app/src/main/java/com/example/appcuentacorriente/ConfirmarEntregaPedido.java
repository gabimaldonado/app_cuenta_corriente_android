package com.example.appcuentacorriente;

import java.util.List;

// Lo que se manda a confirmar_entrega.php (PUT): {"entrega_id": 4, "items": [10]}
// "items" son los ids de entrega_items que la clienta SE QUEDA (no los ids de las prendas).
// Si devuelve todo, la lista va vacía.
public class ConfirmarEntregaPedido {
    int entrega_id;
    List<Integer> items;

    public ConfirmarEntregaPedido(int entrega_id, List<Integer> items) {
        this.entrega_id = entrega_id;
        this.items = items;
    }
}
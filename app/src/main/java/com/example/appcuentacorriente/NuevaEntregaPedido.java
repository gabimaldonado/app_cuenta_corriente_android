package com.example.appcuentacorriente;

import java.util.List;

// Lo que se manda a entregas.php (POST): {"cliente_id": 1, "productos": [1, 2]}
public class NuevaEntregaPedido {
    int cliente_id;
    List<Integer> productos;   // ids de las prendas que se lleva a probar

    public NuevaEntregaPedido(int cliente_id, List<Integer> productos) {
        this.cliente_id = cliente_id;
        this.productos = productos;
    }
}
package com.example.appcuentacorriente;

// Lo que devuelve confirmar_entrega.php:
// {"entrega_id": 4, "estado": "cerrada", "comprados": 1, "devueltos": 1, "total_comprado": 45000.0}
public class ConfirmarEntregaRespuesta {
    int entrega_id;
    String estado;
    int comprados;
    int devueltos;
    double total_comprado;
}
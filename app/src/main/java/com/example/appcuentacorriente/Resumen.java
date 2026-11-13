package com.example.appcuentacorriente;

import java.util.List;

public class Resumen {
    private double total_compras;
    private double total_pagos;
    private double saldo_actual;
    private List<Pago> pagos;

    private Cliente cliente;

    public Cliente getCliente() { return cliente; }
    public double getTotal_compras() { return total_compras; }
    public double getTotal_pagos() { return total_pagos; }
    public double getSaldo_actual() { return saldo_actual; }
    public List<Pago> getPagos() { return pagos; }
}

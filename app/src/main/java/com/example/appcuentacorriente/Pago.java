package com.example.appcuentacorriente;

public class Pago {
    private int id;
    private int cliente_id;
    private String fecha;
    private double monto;
    private String medio;
    private String observacion;

    public Pago() {}

    public Pago(int cliente_id, double monto, String medio, String observacion) {
        this.cliente_id = cliente_id;
        this.monto = monto;
        this.medio = medio;
        this.observacion = observacion;
    }

    public int getId() { return id; }
    public int getCliente_id() { return cliente_id; }
    public String getFecha() { return fecha; }
    public double getMonto() { return monto; }
    public String getMedio() { return medio; }
    public String getObservacion() { return observacion; }
}

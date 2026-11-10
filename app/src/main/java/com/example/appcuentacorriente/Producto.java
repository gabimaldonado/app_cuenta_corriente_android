package com.example.appcuentacorriente;

import java.util.Locale;
public class Producto {
    public int id;
    public String descripcion;
    public String talle;
    public String color;
    public double precio;
    public int stock;
    public String foto;
    public int activo;

    public Producto() {
    }

    @Override
    public String toString() {
        String talleTxt = (talle != null && !talle.isEmpty())? "Talle" + talle : "";
        String colorTxt = (color != null && !color.isEmpty()) ? color : "";
        String detalle = talleTxt;
        if (!detalle.isEmpty() && !colorTxt.isEmpty()) {
            detalle += " · " + colorTxt;
        } else if (detalle.isEmpty()) {
            detalle = colorTxt;
        }
        String stockTxt = (stock > 0) ? "Stock: " + stock : "SIN STOCK";
        return descripcion + " (" + detalle + ")\nPrecio: $" + String.format(Locale.US, "%.2f", precio) + " — " + stockTxt;
    }
}

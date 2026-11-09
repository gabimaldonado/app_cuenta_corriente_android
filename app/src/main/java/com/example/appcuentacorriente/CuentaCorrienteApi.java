package com.example.appcuentacorriente;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import java.util.List;
import retrofit2.http.GET;
import retrofit2.http.Query;

// La lista de endpoints de nuestra API.
// Cada integrante agrega los suyos en su sección.
public interface CuentaCorrienteApi {

    // --- Integrante 1: login y sesión ---
    @POST("login.php")
    Call<LoginRespuesta> login(@Body LoginPedido datos);

    @DELETE("sesion.php")
    Call<Void> cerrarSesion();

    // --- Integrante 2: clientes y catálogo ---
    @GET("clientes.php")
    Call<List<Cliente>> obtenerClientes(@Query("buscar") String stock);

    @POST("clientes.php")
    Call<Cliente> crearCliente(@Body Cliente cliente);

    @GET("productos.php")
    Call<List <Producto>> obtenerProductos(@Query("stock")String stock);

    // --- Integrante 3: entregas ---

    // --- Integrante 4: pagos y resumen ---

    // --- Integrante 5: inicio ---
}
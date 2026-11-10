package com.example.appcuentacorriente;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
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

    // --- Integrante 3: entregas ---

    // --- Integrante 4: pagos y resumen ---

    // --- Integrante 5: inicio ---

    @GET("resumen.php?deudores=1")
    Call<RespuestaResumenDeudores> getResumen();

    @GET("entregas.php")
    Call<List<RespuestaEntregas>> getEntregas(@Query("estado") String estado);

    @GET("clientes.php")
    Call<Cliente> getCliente(@Query("id") int id);
}
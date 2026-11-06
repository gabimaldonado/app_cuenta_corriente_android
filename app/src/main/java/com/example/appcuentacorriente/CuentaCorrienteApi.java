package com.example.appcuentacorriente;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;

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
}
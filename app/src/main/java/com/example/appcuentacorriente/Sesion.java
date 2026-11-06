package com.example.appcuentacorriente;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

// Guarda el token en el teléfono, como el localStorage de la web.
// Sigue guardado aunque se cierre la app.
public class Sesion {

    private static SharedPreferences preferencias(Context contexto) {
        return contexto.getSharedPreferences("sesion", Context.MODE_PRIVATE);
    }

    public static void guardar(Context contexto, String token, String nombre) {
        preferencias(contexto).edit()
                .putString("token", token)
                .putString("nombre", nombre)
                .apply();
    }

    public static String getToken(Context contexto) {
        return preferencias(contexto).getString("token", null);
    }

    public static String getNombre(Context contexto) {
        return preferencias(contexto).getString("nombre", "");
    }

    public static void cerrar(Context contexto) {
        preferencias(contexto).edit().clear().apply();
    }

    // Si la API responde 401 (token vencido o inválido): borra la sesión,
    // avisa y vuelve al login, sin dejar pantallas viejas en la pila.
    // Uso en cualquier pantalla, dentro de onResponse:
    //     if (respuesta.code() == 401) { Sesion.vencida(MiActivity.this); return; }
    public static void vencida(Activity pantalla) {
        cerrar(pantalla);
        Toast.makeText(pantalla, "Tu sesión venció. Ingresá de nuevo.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(pantalla, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pantalla.startActivity(intent);
        pantalla.finish();
    }
}
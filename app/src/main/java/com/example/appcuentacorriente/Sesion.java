package com.example.appcuentacorriente;

import android.content.Context;
import android.content.SharedPreferences;

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
}
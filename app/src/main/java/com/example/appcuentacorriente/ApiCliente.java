package com.example.appcuentacorriente;

import android.content.Context;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Arma Retrofit una sola vez. Toda la app pide la API con ApiCliente.get(this).
public class ApiCliente {

    // La única dirección de la API en toda la app
    private static final String URL_BASE =
            "https://reliable-spirit-production-e065.up.railway.app/api/";

    private static CuentaCorrienteApi api;

    public static CuentaCorrienteApi get(Context contexto) {
        if (api == null) {
            Context app = contexto.getApplicationContext();

            // Antes de cada pedido, si hay token guardado, lo agrega:
            // Authorization: Bearer <token>   (lo que pide auth.php)
            OkHttpClient cliente = new OkHttpClient.Builder()
                    .addInterceptor(cadena -> {
                        Request pedido = cadena.request();
                        String token = Sesion.getToken(app);
                        if (token != null) {
                            pedido = pedido.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build();
                        }
                        return cadena.proceed(pedido);
                    })
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL_BASE)
                    .client(cliente)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            api = retrofit.create(CuentaCorrienteApi.class);
        }
        return api;
    }

    // Saca el mensaje del {"error": "..."} que manda la API (400, 401, 404, 422)
    public static String mensajeDeError(Response<?> respuesta) {
        try {
            ErrorApi e = new Gson().fromJson(respuesta.errorBody().charStream(), ErrorApi.class);
            if (e != null && e.error != null) {
                return e.error;
            }
        } catch (Exception ignorado) {
            // Si no vino un JSON con "error", usamos el código
        }
        return "Error " + respuesta.code();
    }

    private static class ErrorApi {
        String error;
    }
}
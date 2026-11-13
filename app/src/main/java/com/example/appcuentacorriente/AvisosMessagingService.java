package com.example.appcuentacorriente;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class AvisosMessagingService extends FirebaseMessagingService {

    // El token identifica a ESTA instalación de la app: el servidor lo usa como dirección
    @Override
    public void onNewToken(String token) {
        // En teoría aquí le notificaríamos al backend sobre este dispositivo,
        //y el mismo guardaría el token en la base para asociarlo a alguna cuenta y poder mandarle una notificación a esa persona en particular
        // Como en este trabajo nos dispusimos a no cambiar el backend, esa lógica no está implementada.
        //Simplemente, utilizaremos la consola de firebase como utilidad para enviar las notificaciones
        Log.d("FCM", "Token nuevo: " + token);
    }

    // Llega acá si la app está en primer plano, o si el mensaje es sólo de datos.
    // Con la app en segundo plano, Android muestra la notificación solo.
    @Override
    public void onMessageReceived(RemoteMessage mensaje) {
        if (mensaje.getNotification() != null) {
            String titulo = mensaje.getNotification().getTitle();
            String body = mensaje.getNotification().getBody();

            //nos conectamos al hilo principal y le decimos que corra nuestra función y cree el toast de la notificación. (en este hilo no nos deja)
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(
                        getApplicationContext(),
                        titulo + ": " + body,
                        Toast.LENGTH_LONG
                ).show();
            });
        }
    }

}

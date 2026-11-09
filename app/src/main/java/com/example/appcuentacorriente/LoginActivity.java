package com.example.appcuentacorriente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private TextView tvMensaje;
    private Button btnIngresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si ya hay un token guardado, no hace falta loguearse de nuevo
        if (Sesion.getToken(this) != null) {
            irAlMenu();
            return;
        }

        // --- Generado por Android Studio: no se toca ---
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Buscar las vistas por su id ---
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvMensaje = findViewById(R.id.tvMensaje);
        btnIngresar = findViewById(R.id.btnIngresar);

        btnIngresar.setOnClickListener(v -> ingresar());

        // Si se giró la pantalla, recuperamos el mensaje que había
        if (savedInstanceState != null) {
            tvMensaje.setText(savedInstanceState.getString("mensaje"));
        }
    }

    private void ingresar() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // 1. Validar antes de enviar nada
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("La contraseña es obligatoria");
            etPassword.requestFocus();
            return;
        }

        // 2. Mientras espera: botón desactivado y "Cargando…" (no se envía dos veces)
        cargando(true);
        tvMensaje.setText("");

        // 3. Pedirle el token a login.php
        ApiCliente.get(this).login(new LoginPedido(email, password))
                .enqueue(new Callback<LoginRespuesta>() {

                    @Override
                    public void onResponse(Call<LoginRespuesta> call, Response<LoginRespuesta> respuesta) {
                        cargando(false);
                        if (respuesta.isSuccessful() && respuesta.body() != null) {
                            LoginRespuesta datos = respuesta.body();
                            Sesion.guardar(LoginActivity.this, datos.token, datos.nombre);
                            Toast.makeText(LoginActivity.this, "¡Bienvenida!", Toast.LENGTH_SHORT).show();
                            irAlMenu();
                        } else {
                            // 400 o 401: el mensaje que manda la API
                            tvMensaje.setText(ApiCliente.mensajeDeError(respuesta));
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginRespuesta> call, Throwable error) {
                        cargando(false);
                        tvMensaje.setText("No se pudo conectar con el servidor. Revisá tu conexión.");
                    }
                });
    }

    // Abre la pantalla principal y cierra el login (así no queda en la pila)
    private void irAlMenu() {
        // PROVISORIO: abre Clientas. Cuando Tadeo suba Inicio, cambiar por InicioActivity.class
        Intent intent = new Intent(this, ClientesActivity.class);
        startActivity(intent);
        finish();
    }

    // Activa o desactiva el botón mientras se espera la respuesta
    private void cargando(boolean estaCargando) {
        btnIngresar.setEnabled(!estaCargando);
        btnIngresar.setText(estaCargando ? "Cargando…" : getString(R.string.btn_ingresar));
    }

    // Antes de destruir la pantalla (por ejemplo, al girar), guardamos el mensaje
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tvMensaje != null) {
            outState.putString("mensaje", tvMensaje.getText().toString());
        }
    }
}
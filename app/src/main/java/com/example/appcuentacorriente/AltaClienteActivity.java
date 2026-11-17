package com.example.appcuentacorriente;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AltaClienteActivity extends AppCompatActivity  {
    private EditText etNombre;
    private EditText etTelefono;
    private EditText etDni;
    private EditText etDireccion;
    private EditText etCiudad;
    private EditText etNotas;
    private TextView tvErrorAlta;
    private Button btnGuardar;

    private boolean enviando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alta_cliente);
        Utils.setupInsetsConPaddingOriginal(this);

        etNombre = findViewById(R.id.etNombreCliente);
        etTelefono = findViewById(R.id.etTelefonoCliente);
        etDni = findViewById(R.id.etDniCliente);
        etDireccion = findViewById(R.id.etDireccionCliente);
        etCiudad = findViewById(R.id.etCiudadCliente);
        etNotas = findViewById(R.id.etNotasCliente);
        tvErrorAlta = findViewById(R.id.tvErrorAlta);
        btnGuardar = findViewById(R.id.btnGuardarCliente);

        // R5: Restaurar mensaje de error si se giró la pantalla
        if (savedInstanceState != null) {
            String errorPrevio = savedInstanceState.getString("error_alta");
            if (errorPrevio != null && !errorPrevio.isEmpty()) {
                tvErrorAlta.setText(errorPrevio);
                tvErrorAlta.setVisibility(View.VISIBLE);
            }
        }

        btnGuardar.setOnClickListener(v -> {
            if (!enviando) {
                validarYEnviar();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tvErrorAlta.getVisibility() == View.VISIBLE) {
            outState.putString("error_alta", tvErrorAlta.getText().toString());
        }
    }

    private void validarYEnviar() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String ciudad = etCiudad.getText().toString().trim();
        String notas = etNotas.getText().toString().trim();

        if (nombre.isEmpty()) {
            mostrarError(getString(R.string.err_nombre_obligatorio));
            return;
        }
        if (!telefono.matches("^\\d{10,15}$")) {
            mostrarError(getString(R.string.err_telefono_invalido));
            return;
        }
        if (!dni.isEmpty() && !dni.matches("^\\d{7,8}$")) {
            mostrarError(getString(R.string.err_dni_invalido));
            return;
        }

        tvErrorAlta.setVisibility(View.GONE);
        enviando = true;
        btnGuardar.setEnabled(false);

        Cliente nuevaClienta = new Cliente(nombre, telefono, dni, direccion, ciudad, notas);

        ApiCliente.get(this).crearCliente(nuevaClienta).enqueue(new Callback<Cliente>() {
            @Override
            public void onResponse(Call<Cliente> call, Response<Cliente> response) {
                enviando = false;
                btnGuardar.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(AltaClienteActivity.this, R.string.msg_clienta_registrada, Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    mostrarError(ApiCliente.mensajeDeError(response));
                }
            }

            @Override
            public void onFailure(Call<Cliente> call, Throwable t) {
                enviando = false;
                btnGuardar.setEnabled(true);
                mostrarError(getString(R.string.err_conexion_servidor));
            }
        });
    }

    private void mostrarError(String mensaje) {
        tvErrorAlta.setText(mensaje);
        tvErrorAlta.setVisibility(View.VISIBLE);
    }
}

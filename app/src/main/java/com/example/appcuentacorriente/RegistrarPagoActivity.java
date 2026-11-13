package com.example.appcuentacorriente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrarPagoActivity extends AppCompatActivity {

    private TextView tvClientaNombre, tvSaldoActual;
    private TextInputEditText etMonto, etObservacion;
    private Spinner spMedioPago;
    private ProgressBar progressBar;
    private Button btnConfirmarPago;

    private int clienteId;
    private double saldoActual;
    private String clienteNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_pago);

        //Vincular componentes de la vista XML
        tvClientaNombre = findViewById(R.id.tvClientaNombre);
        tvSaldoActual = findViewById(R.id.tvSaldoActual);
        etMonto = findViewById(R.id.etMonto);
        etObservacion = findViewById(R.id.etObservacion);
        spMedioPago = findViewById(R.id.spMedioPago);
        progressBar = findViewById(R.id.progressBar);
        btnConfirmarPago = findViewById(R.id.btnConfirmarPago);

        //  Poblar el Spinner con los medios de pago permitidos en la base de datos
        String[] medios = new String[]{"efectivo", "transferencia", "tarjeta", "otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, medios);
        spMedioPago.setAdapter(adapter);

        // Recibir datos enviados desde FichaClienteActivity por Intent Extras
        Intent intent = getIntent();
        clienteId = intent.getIntExtra("cliente_id", -1);
        clienteNombre = intent.getStringExtra("cliente_nombre");
        saldoActual = intent.getDoubleExtra("cliente_saldo", 0.0);

        if (clienteNombre != null) {
            tvClientaNombre.setText("Clienta: " + clienteNombre);
        } else {
            tvClientaNombre.setText("Clienta ID: " + clienteId);
        }
        tvSaldoActual.setText(String.format("Saldo adeudado: $%.2f", saldoActual));

        // Acción del botón de confirmación
        btnConfirmarPago.setOnClickListener(v -> intentarRegistrarPago());
    }

    private void intentarRegistrarPago() {
        String montoTexto = etMonto.getText() != null ? etMonto.getText().toString().trim() : "";

        //Validación antes de enviar (en el cliente)
        if (montoTexto.isEmpty()) {
            etMonto.setError("Ingrese un monto válido");
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoTexto);
        } catch (NumberFormatException e) {
            etMonto.setError("Monto inválido");
            return;
        }

        if (monto <= 0) {
            etMonto.setError("El monto debe ser mayor a 0");
            return;
        }

        if (saldoActual > 0 && monto > saldoActual) {
            etMonto.setError("El monto no puede superar el saldo adeudado ($" + saldoActual + ")");
            return;
        }

        String medio = spMedioPago.getSelectedItem().toString();
        String observacion = etObservacion.getText() != null ? etObservacion.getText().toString().trim() : null;

        // Retroalimentación de espera y evitar doble click
        mostrarCargando(true);

        // Preparar objeto para enviar a la API
        Pago nuevoPago = new Pago(clienteId, monto, medio, observacion);

        // Consumir POST api/pagos.php fuera del hilo principal usando Retrofit
        CuentaCorrienteApi api = ApiCliente.get(this);
        api.registrarPago(nuevoPago).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                mostrarCargando(false);

                if (response.isSuccessful()) {
                    Toast.makeText(RegistrarPagoActivity.this, "¡Pago registrado correctamente!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    // Leer el error real que manda Railway (400, 422, 500, etc.)
                    String mensajeError = ApiCliente.mensajeDeError(response);
                    android.util.Log.e("PAGO_API_ERROR", "Código HTTP: " + response.code() + " | Mensaje: " + mensajeError);
                    Toast.makeText(RegistrarPagoActivity.this, "Error " + response.code() + ": " + mensajeError, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Manejo sin cierres con modo avión o API caída
                mostrarCargando(false);
                Toast.makeText(RegistrarPagoActivity.this, "No se pudo conectar con el servidor. Revise su conexión.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarCargando(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnConfirmarPago.setEnabled(!cargando);
        etMonto.setEnabled(!cargando);
        spMedioPago.setEnabled(!cargando);
        etObservacion.setEnabled(!cargando);
    }
}
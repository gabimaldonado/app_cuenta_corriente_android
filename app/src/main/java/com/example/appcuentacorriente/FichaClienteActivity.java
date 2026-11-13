package com.example.appcuentacorriente;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FichaClienteActivity extends AppCompatActivity {

    private TextView tvNombreCliente, tvTelefonoCliente, tvSaldoMonto, tvDesgloseTotales;
    private Button btnIrAPagar, btnWhatsapp;
    private ListView lvPagos;
    private ProgressBar pbCargando;

    private int clienteId;
    private String clienteNombre = "";
    private String clienteTelefono = "";
    private double saldoActual = 0.0;

    // Launcher para esperar el resultado de RegistrarPagoActivity
    private final ActivityResultLauncher<Intent> pagoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Si se guardó el pago, recarga la ficha para actualizar el saldo en vivo
                    cargarFichaDesdeApi();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ficha_cliente);

        tvNombreCliente = findViewById(R.id.tvNombreCliente);
        tvTelefonoCliente = findViewById(R.id.tvTelefonoCliente);
        tvSaldoMonto = findViewById(R.id.tvSaldoMonto);
        tvDesgloseTotales = findViewById(R.id.tvDesgloseTotales);
        btnIrAPagar = findViewById(R.id.btnIrAPagar);
        btnWhatsapp = findViewById(R.id.btnWhatsapp);
        lvPagos = findViewById(R.id.lvPagos);
        pbCargando = findViewById(R.id.pbCargando);

        // Recibir ID por extras
        clienteId = getIntent().getIntExtra("cliente_id", -1);
        if (clienteId == -1) {
            // Intentar leer por si viene como "id"
            clienteId = getIntent().getIntExtra("id", -1);
        }

        if (clienteId == -1) {
            Toast.makeText(this, "ID de clienta no recibido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar botón para abrir RegistrarPago esperando resultado
        btnIrAPagar.setOnClickListener(v -> {
            Intent intent = new Intent(FichaClienteActivity.this, RegistrarPagoActivity.class);
            intent.putExtra("cliente_id", clienteId);
            intent.putExtra("cliente_nombre", clienteNombre);
            intent.putExtra("cliente_saldo", saldoActual);
            pagoLauncher.launch(intent);
        });

        // Configurar Intent implícito para abrir WhatsApp
        btnWhatsapp.setOnClickListener(v -> {
            if (clienteTelefono.isEmpty()) {
                Toast.makeText(this, "La clienta no tiene teléfono registrado", Toast.LENGTH_SHORT).show();
                return;
            }
            String mensaje = "Hola " + clienteNombre + ", tu saldo actual es de $" + saldoActual;
            String url = "https://wa.me/" + clienteTelefono.replace("+", "").replace(" ", "") + "?text=" + Uri.encode(mensaje);
            Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(sendIntent);
        });

        cargarFichaDesdeApi();
    }

    private void cargarFichaDesdeApi() {
        pbCargando.setVisibility(View.VISIBLE);

        CuentaCorrienteApi api = ApiCliente.get(this);
        api.obtenerResumenCliente(clienteId).enqueue(new Callback<Resumen>() {
            @Override
            public void onResponse(Call<Resumen> call, Response<Resumen> response) {
                pbCargando.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Resumen resumen = response.body();
                    if (resumen.getCliente() != null) {
                        clienteNombre = resumen.getCliente().nombre;
                        clienteTelefono = resumen.getCliente().telefono;
                        tvNombreCliente.setText(clienteNombre);
                        tvTelefonoCliente.setText("Teléfono: " + (clienteTelefono != null ? clienteTelefono : "-"));
                    }
                    saldoActual = resumen.getSaldo_actual();

                    tvSaldoMonto.setText(String.format("$ %.2f", saldoActual));
                    tvDesgloseTotales.setText(String.format("Compras: $%.2f | Pagos: $%.2f",
                            resumen.getTotal_compras(), resumen.getTotal_pagos()));

                    // Mostrar lista de pagos
                    List<String> listadoTextos = new ArrayList<>();
                    if (resumen.getPagos() != null && !resumen.getPagos().isEmpty()) {
                        for (Pago p : resumen.getPagos()) {
                            listadoTextos.add(String.format("%s: $%.2f (%s)", p.getFecha(), p.getMonto(), p.getMedio()));
                        }
                    } else {
                        listadoTextos.add("No registra pagos previos.");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            FichaClienteActivity.this,
                            android.R.layout.simple_list_item_1,
                            listadoTextos
                    );
                    lvPagos.setAdapter(adapter);

                } else if (response.code() == 404) {
                    Toast.makeText(FichaClienteActivity.this, "Clienta no encontrada en la API", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(FichaClienteActivity.this, ApiCliente.mensajeDeError(response), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Resumen> call, Throwable t) {
                pbCargando.setVisibility(View.GONE);
                Toast.makeText(FichaClienteActivity.this, "No se pudo conectar con el servidor", Toast.LENGTH_LONG).show();
            }
        });
    }
}
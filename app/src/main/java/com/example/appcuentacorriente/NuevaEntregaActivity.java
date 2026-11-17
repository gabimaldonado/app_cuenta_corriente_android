package com.example.appcuentacorriente;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Nueva entrega: se elige la clienta y las prendas que se lleva a probar.
// Tocar una clienta la elige. Tocar una prenda con stock la agrega; tocarla en "Se lleva a probar" la quita.
public class NuevaEntregaActivity extends AppCompatActivity {

    // Para filtrar Logcat por los mensajes de la red: tag:API
    private static final String TAG = "API";

    private TextView tvClientaElegida;
    private TextView tvTituloElegidas;
    private TextView tvTotal;
    private TextView tvMensaje;
    private Button btnGuardar;

    // Las tres listas de datos y sus adaptadores (el adaptador usa la misma lista que le pasamos)
    private final List<Cliente> clientas = new ArrayList<>();
    private final List<Producto> disponibles = new ArrayList<>();
    private final List<Producto> elegidas = new ArrayList<>();
    private ArrayAdapter<Cliente> adaptadorClientas;
    private ArrayAdapter<Producto> adaptadorDisponibles;
    private ArrayAdapter<Producto> adaptadorElegidas;

    private Cliente clientaElegida;           // null hasta que se toca una clienta
    private boolean clientasCargadas = false;
    private boolean prendasCargadas = false;

    // Lo que había elegido antes de girar la pantalla (se aplica cuando llegan las listas)
    private int clientaGuardada = 0;
    private String elegidasGuardado = ",";    // ids de las prendas elegidas. Formato: ",3,5,"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nueva_entrega);
        Utils.setupInsetsConPaddingOriginal(this);

        // --- Buscar las vistas por su id ---
        tvClientaElegida = findViewById(R.id.tvClientaElegida);
        tvTituloElegidas = findViewById(R.id.tvTituloElegidas);
        tvTotal = findViewById(R.id.tvTotal);
        tvMensaje = findViewById(R.id.tvMensaje);
        btnGuardar = findViewById(R.id.btnGuardar);
        ListView lvClientas = findViewById(R.id.lvClientas);
        ListView lvPrendas = findViewById(R.id.lvPrendas);
        ListView lvPrendasElegidas = findViewById(R.id.lvPrendasElegidas);

        // --- Las listas: cada fila muestra el toString() del modelo ---
        adaptadorClientas = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, clientas);
        adaptadorDisponibles = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, disponibles);
        adaptadorElegidas = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, elegidas);
        lvClientas.setAdapter(adaptadorClientas);
        lvPrendas.setAdapter(adaptadorDisponibles);
        lvPrendasElegidas.setAdapter(adaptadorElegidas);

        // Tocar una clienta: queda elegida
        lvClientas.setOnItemClickListener((lista, fila, posicion, idFila) -> {
            clientaElegida = clientas.get(posicion);
            mostrarClienta();
        });

        // Tocar una prenda disponible: pasa a "Se lleva a probar"
        lvPrendas.setOnItemClickListener((lista, fila, posicion, idFila) -> {
            Producto prenda = disponibles.get(posicion);
            disponibles.remove(posicion);
            elegidas.add(prenda);
            actualizarPrendas();
        });

        // Tocar una prenda elegida: vuelve a la lista de disponibles
        lvPrendasElegidas.setOnItemClickListener((lista, fila, posicion, idFila) -> {
            Producto prenda = elegidas.remove(posicion);
            disponibles.add(prenda);
            actualizarPrendas();
        });

        // El botón queda desactivado hasta que lleguen las clientas y las prendas
        btnGuardar.setEnabled(false);
        btnGuardar.setOnClickListener(v -> guardar());

        // Si se giró la pantalla, recuperamos lo que había elegido
        if (savedInstanceState != null) {
            clientaGuardada = savedInstanceState.getInt("clienta");
            String guardado = savedInstanceState.getString("elegidas");
            if (guardado != null) {
                elegidasGuardado = guardado;
            }
        }

        actualizarPrendas();
        mostrarClienta();

        // Al abrir (y al girar), las dos listas se piden a la API
        tvMensaje.setText(R.string.entrega_cargando_listas);
        cargarClientas();
        cargarPrendas();
    }

    // GET clientes.php: todas las clientas activas
    private void cargarClientas() {
        ApiCliente.get(this).obtenerClientes(null).enqueue(new Callback<List<Cliente>>() {

            @Override
            public void onResponse(Call<List<Cliente>> call, Response<List<Cliente>> respuesta) {
                if (respuesta.code() == 401) {
                    Sesion.vencida(NuevaEntregaActivity.this);
                    return;
                }
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    clientas.clear();
                    clientas.addAll(respuesta.body());
                    adaptadorClientas.notifyDataSetChanged();

                    // Si había una clienta elegida antes de girar, la volvemos a elegir
                    if (clientaGuardada != 0) {
                        for (Cliente c : clientas) {
                            if (c.id != null && c.id == clientaGuardada) {
                                clientaElegida = c;
                            }
                        }
                        mostrarClienta();
                    }
                    clientasCargadas = true;
                    terminoLaCarga();
                } else {
                    tvMensaje.setText(ApiCliente.mensajeDeError(respuesta));
                }
            }

            @Override
            public void onFailure(Call<List<Cliente>> call, Throwable error) {
                Log.e(TAG, "No se pudieron cargar las clientas", error);
                tvMensaje.setText(R.string.err_conexion_servidor);
            }
        });
    }

    // GET productos.php?stock=con: solo las prendas que tienen stock
    private void cargarPrendas() {
        ApiCliente.get(this).obtenerProductos("con").enqueue(new Callback<List<Producto>>() {

            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> respuesta) {
                if (respuesta.code() == 401) {
                    Sesion.vencida(NuevaEntregaActivity.this);
                    return;
                }
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    disponibles.clear();
                    disponibles.addAll(respuesta.body());

                    // Las prendas que estaban elegidas antes de girar pasan a "Se lleva a probar"
                    elegidas.clear();
                    for (int i = disponibles.size() - 1; i >= 0; i--) {
                        if (elegidasGuardado.contains("," + disponibles.get(i).id + ",")) {
                            elegidas.add(0, disponibles.remove(i));
                        }
                    }
                    actualizarPrendas();
                    prendasCargadas = true;
                    terminoLaCarga();
                } else {
                    tvMensaje.setText(ApiCliente.mensajeDeError(respuesta));
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable error) {
                Log.e(TAG, "No se pudieron cargar las prendas", error);
                tvMensaje.setText(R.string.err_conexion_servidor);
            }
        });
    }

    // Cuando llegaron las dos listas, se puede guardar
    private void terminoLaCarga() {
        if (clientasCargadas && prendasCargadas) {
            tvMensaje.setText("");
            btnGuardar.setEnabled(true);
        }
    }

    // Muestra qué clienta está elegida
    private void mostrarClienta() {
        if (clientaElegida == null) {
            tvClientaElegida.setText(R.string.entrega_clienta_sin_elegir);
        } else {
            tvClientaElegida.setText(getString(R.string.entrega_clienta_elegida, clientaElegida.nombre));
        }
    }

    // Refresca las dos listas de prendas, el título y el total
    private void actualizarPrendas() {
        adaptadorDisponibles.notifyDataSetChanged(); // le avisa al ListView que sus datos cambiaron
        adaptadorElegidas.notifyDataSetChanged();

        double total = 0;
        for (Producto prenda : elegidas) {
            total += prenda.precio;
        }
        tvTituloElegidas.setText(getString(R.string.entrega_titulo_elegidas_cant, elegidas.size()));
        tvTotal.setText(getString(R.string.entrega_total, Math.round(total)));
    }

    // POST entregas.php: crea la entrega con la clienta y los ids de las prendas
    private void guardar() {
        // 1. Validar antes de enviar nada
        if (clientaElegida == null) {
            tvMensaje.setText(R.string.err_elegir_clienta);
            return;
        }
        if (elegidas.isEmpty()) {
            tvMensaje.setText(R.string.err_elegir_prenda);
            return;
        }

        List<Integer> idsPrendas = new ArrayList<>();
        for (Producto prenda : elegidas) {
            idsPrendas.add(prenda.id);
        }

        // 2. Mientras espera: botón desactivado (no se puede guardar dos veces)
        cargando(true);
        tvMensaje.setText("");

        // 3. Mandar el pedido
        ApiCliente.get(this).crearEntrega(new NuevaEntregaPedido(clientaElegida.id, idsPrendas))
                .enqueue(new Callback<Entrega>() {

                    @Override
                    public void onResponse(Call<Entrega> call, Response<Entrega> respuesta) {
                        if (respuesta.code() == 401) {
                            Sesion.vencida(NuevaEntregaActivity.this);
                            return;
                        }
                        cargando(false);
                        if (respuesta.isSuccessful() && respuesta.body() != null) {
                            Toast.makeText(NuevaEntregaActivity.this,
                                    getString(R.string.msg_entrega_guardada, elegidas.size()),
                                    Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            // 400 (por ejemplo "No hay stock de la prenda...") o 404: el mensaje que manda la API
                            tvMensaje.setText(ApiCliente.mensajeDeError(respuesta));
                        }
                    }

                    @Override
                    public void onFailure(Call<Entrega> call, Throwable error) {
                        cargando(false);
                        Log.e(TAG, "No se pudo guardar la entrega", error);
                        tvMensaje.setText(R.string.err_conexion_no_guardo);
                    }
                });
    }

    // Activa o desactiva el botón mientras se espera la respuesta
    private void cargando(boolean estaCargando) {
        btnGuardar.setEnabled(!estaCargando);
        btnGuardar.setText(estaCargando ? "Guardando…" : getString(R.string.entrega_btn_guardar));
    }

    // Antes de destruir la pantalla (por ejemplo, al girar), guardamos lo que se eligió
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int idClienta = clientaGuardada; // si la lista todavía no llegó, se conserva lo anterior
        if (clientasCargadas) {
            idClienta = (clientaElegida != null && clientaElegida.id != null) ? clientaElegida.id : 0;
        }

        String ids = elegidasGuardado;   // si las prendas todavía no llegaron, se conserva lo anterior
        if (prendasCargadas) {
            ids = ",";
            for (Producto prenda : elegidas) {
                ids = ids + prenda.id + ",";
            }
        }

        outState.putInt("clienta", idClienta);
        outState.putString("elegidas", ids);
    }
}
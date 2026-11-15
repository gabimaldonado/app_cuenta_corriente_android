package com.example.appcuentacorriente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Confirmar compra: la clienta vuelve y se decide qué prenda se queda y cuál devuelve.
// Recibe por extra SOLO el id de la entrega; el resto se pide a la API (GET entregas.php?id=).
public class ConfirmarEntregaActivity extends AppCompatActivity {

    // Claves de los extras: constantes, para no escribirlas dos veces (Semana 13)
    // EXTRA_ENTREGA_ID tiene que ser la misma clave que usa InicioActivity al abrir esta pantalla.
    public static final String EXTRA_ENTREGA_ID = "idEntrega";
    public static final String EXTRA_COMPRADOS = "com.example.appcuentacorriente.COMPRADOS";
    public static final String EXTRA_DEVUELTOS = "com.example.appcuentacorriente.DEVUELTOS";

    // Para filtrar Logcat por los mensajes de la red: tag:API
    private static final String TAG = "API";

    // SOLO PARA PROBAR SIN EL MENÚ: poné acá el id de una entrega abierta.
    // Antes de subir el código, dejalo en 0.
    private static final int ID_PRUEBA = 0;

    private TextView tvEntrega;
    private TextView tvResumen;
    private TextView tvTotalCompra;
    private TextView tvMensaje;
    private Button btnConfirmar;
    private ListView lvItems;

    // Las filas que se ven en la lista, y el adaptador que las pone en pantalla
    private final List<String> filas = new ArrayList<>();
    private ArrayAdapter<String> adaptador;

    private Entrega entrega;                              // llega de la API
    private List<EntregaItem> items = new ArrayList<>();  // las prendas de la entrega
    private boolean[] seQueda;                            // por cada prenda: true = se queda, false = devuelve

    // Ids de los ítems que devuelve, para no perderlos al girar. Formato: ",12,14,"
    private String devuelvenGuardado = ",";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // --- Generado por Android Studio: no se toca ---
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirmar_entrega);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Buscar las vistas por su id ---
        tvEntrega = findViewById(R.id.tvEntrega);
        tvResumen = findViewById(R.id.tvResumen);
        tvTotalCompra = findViewById(R.id.tvTotalCompra);
        tvMensaje = findViewById(R.id.tvMensaje);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        lvItems = findViewById(R.id.lvItems);

        // La lista: cada fila muestra un texto de la lista "filas"
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filas);
        lvItems.setAdapter(adaptador);

        // Tocar una prenda alterna entre "Se queda" y "Devuelve"
        lvItems.setOnItemClickListener((lista, fila, posicion, idFila) -> {
            if (seQueda != null) {
                seQueda[posicion] = !seQueda[posicion];
                actualizarPantalla();
            }
        });

        // El botón queda desactivado hasta que llegue la entrega
        btnConfirmar.setEnabled(false);
        btnConfirmar.setOnClickListener(v -> confirmar());

        // Si se giró la pantalla, recuperamos qué prendas estaban marcadas como "devuelve"
        if (savedInstanceState != null) {
            String guardado = savedInstanceState.getString("devuelven");
            if (guardado != null) {
                devuelvenGuardado = guardado;
            }
        }

        // Por el extra llega SOLO el id de la entrega
        int idEntrega = getIntent().getIntExtra(EXTRA_ENTREGA_ID, ID_PRUEBA);
        if (idEntrega <= 0) {
            tvMensaje.setText("Falta el id de la entrega");
            return;
        }
        cargarEntrega(idEntrega); // al abrir (y al girar), la entrega se pide a la API
    }

    // GET entregas.php?id=...: trae la entrega con sus prendas
    private void cargarEntrega(int idEntrega) {
        tvEntrega.setText("Cargando…");
        ApiCliente.get(this).obtenerEntrega(idEntrega).enqueue(new Callback<Entrega>() {

            @Override
            public void onResponse(Call<Entrega> call, Response<Entrega> respuesta) {
                if (respuesta.code() == 401) {
                    Sesion.vencida(ConfirmarEntregaActivity.this);
                    return;
                }
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    entrega = respuesta.body();
                    mostrarEntrega();
                } else {
                    // 404: esa entrega no existe
                    tvEntrega.setText("");
                    tvMensaje.setText(ApiCliente.mensajeDeError(respuesta));
                }
            }

            @Override
            public void onFailure(Call<Entrega> call, Throwable error) {
                Log.e(TAG, "No se pudo cargar la entrega", error);
                tvEntrega.setText("");
                tvMensaje.setText("No se pudo conectar con el servidor. Revisá tu conexión.");
            }
        });
    }

    // Cuando llegó la entrega: arma la lista y activa el botón
    private void mostrarEntrega() {
        tvEntrega.setText("Entrega " + entrega.getId() + " · " + entrega.getFecha());

        items = entrega.getEntrega_items();
        if (items == null) {
            items = new ArrayList<>();
        }

        // Por defecto todas se quedan, salvo las que estaban marcadas "devuelve" antes de girar
        seQueda = new boolean[items.size()];
        for (int i = 0; i < items.size(); i++) {
            seQueda[i] = !devuelvenGuardado.contains("," + items.get(i).getId() + ",");
        }

        if ("cerrada".equals(entrega.getEstado())) {
            tvMensaje.setText("Esta entrega ya estaba cerrada");
        } else {
            btnConfirmar.setEnabled(true);
        }
        actualizarPantalla();
    }

    // Vuelve a armar las filas, el resumen y el total según lo que esté marcado
    private void actualizarPantalla() {
        filas.clear();
        int quedan = 0;
        int devuelven = 0;
        double total = 0;

        for (int i = 0; i < items.size(); i++) {
            EntregaItem item = items.get(i);
            String estado;
            if (seQueda[i]) {
                estado = "Se queda";
                quedan++;
                total += item.getPrecio_unitario();
            } else {
                estado = "Devuelve";
                devuelven++;
            }
            filas.add(estado + " · Prenda " + item.getProducto_id()
                    + " · $ " + Math.round(item.getPrecio_unitario()));
        }

        adaptador.notifyDataSetChanged(); // le avisa al ListView que sus datos cambiaron
        tvResumen.setText("Se queda " + quedan + " · Devuelve " + devuelven);
        tvTotalCompra.setText("Se suma al saldo: $ " + Math.round(total));
    }

    // PUT confirmar_entrega.php: manda los ids de los ítems que se quedan
    private void confirmar() {
        List<Integer> idsQueSeQuedan = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (seQueda[i]) {
                idsQueSeQuedan.add(items.get(i).getId());
            }
        }

        // Mientras espera: botón desactivado (no se puede confirmar dos veces)
        cargando(true);
        tvMensaje.setText("");

        ApiCliente.get(this).confirmarEntrega(new ConfirmarEntregaPedido(entrega.getId(), idsQueSeQuedan))
                .enqueue(new Callback<ConfirmarEntregaRespuesta>() {

                    @Override
                    public void onResponse(Call<ConfirmarEntregaRespuesta> call,
                                           Response<ConfirmarEntregaRespuesta> respuesta) {
                        if (respuesta.code() == 401) {
                            Sesion.vencida(ConfirmarEntregaActivity.this);
                            return;
                        }
                        cargando(false);
                        if (respuesta.isSuccessful() && respuesta.body() != null) {
                            ConfirmarEntregaRespuesta datos = respuesta.body();
                            Toast.makeText(ConfirmarEntregaActivity.this,
                                    "Entrega confirmada: se queda " + datos.comprados
                                            + ", devuelve " + datos.devueltos,
                                    Toast.LENGTH_LONG).show();

                            // Volver con resultado, para que la pantalla anterior se actualice
                            Intent resultado = new Intent();
                            resultado.putExtra(EXTRA_ENTREGA_ID, datos.entrega_id);
                            resultado.putExtra(EXTRA_COMPRADOS, datos.comprados);
                            resultado.putExtra(EXTRA_DEVUELTOS, datos.devueltos);
                            setResult(RESULT_OK, resultado);
                            finish();
                        } else {
                            // 400, 404 o 409: el mensaje que manda la API
                            tvMensaje.setText(ApiCliente.mensajeDeError(respuesta));
                            if (respuesta.code() == 409) {
                                // Ya estaba cerrada: confirmar de nuevo no tiene sentido
                                btnConfirmar.setEnabled(false);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ConfirmarEntregaRespuesta> call, Throwable error) {
                        cargando(false);
                        Log.e(TAG, "No se pudo confirmar la entrega", error);
                        tvMensaje.setText("No se pudo conectar con el servidor. La entrega no se confirmó.");
                    }
                });
    }

    // Activa o desactiva el botón mientras se espera la respuesta
    private void cargando(boolean estaCargando) {
        btnConfirmar.setEnabled(!estaCargando);
        btnConfirmar.setText(estaCargando ? "Enviando…" : getString(R.string.entrega_btn_confirmar));
    }

    // Antes de destruir la pantalla (por ejemplo, al girar), guardamos qué prendas devuelve
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String devuelven = devuelvenGuardado; // si la entrega todavía no llegó, se conserva lo anterior
        if (seQueda != null) {
            devuelven = ",";
            for (int i = 0; i < items.size(); i++) {
                if (!seQueda[i]) {
                    devuelven = devuelven + items.get(i).getId() + ",";
                }
            }
        }
        outState.putString("devuelven", devuelven);
    }
}
package com.example.appcuentacorriente;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class ClientesActivity extends AppCompatActivity {
    private EditText etBuscarCliente;
    private Button btnBuscarCliente;
    private Button btnIrAltaCliente;
    private Button btnIrProductos;
    private TextView tvEstadoClientes;
    private ListView lvClientes;

    private final List<Cliente> listaClientes = new ArrayList<>();
    private ArrayAdapter<Cliente> adapter;

    private final ActivityResultLauncher<Intent> altaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    cargarClientes(etBuscarCliente.getText().toString().trim());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_clientes);
        Utils.setupInsetsConPaddingOriginal(this);

        etBuscarCliente = findViewById(R.id.etBuscarCliente);
        btnBuscarCliente = findViewById(R.id.btnBuscarCliente);
        btnIrAltaCliente = findViewById(R.id.btnIrAltaCliente);
        btnIrProductos = findViewById(R.id.btnIrProductos);
        tvEstadoClientes = findViewById(R.id.tvEstadoCliente);
        lvClientes = findViewById(R.id.lvClientes);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaClientes);
        lvClientes.setAdapter(adapter);

        btnBuscarCliente.setOnClickListener(v ->
                cargarClientes(etBuscarCliente.getText().toString().trim())
        );

        btnIrAltaCliente.setOnClickListener(v -> {
            Intent intent = new Intent(this, AltaClienteActivity.class);
            altaLauncher.launch(intent);
        });

        btnIrProductos.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductosActivity.class);
            startActivity(intent);
        });

        // Tocar una clienta abre FichaClienteActivity con su id como extra (Integrante 4)
        lvClientes.setOnItemClickListener((parent, view, position, id) -> {
            Cliente clienta = listaClientes.get(position);
            try {
                Class<?> fichaClass = Class.forName("com.example.appcuentacorriente.FichaClienteActivity");
                Intent intent = new Intent(this, fichaClass);
                intent.putExtra("id", clienta.id);
                intent.putExtra("cliente_id", clienta.id);
                intent.putExtra("nombre", clienta.nombre);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                Toast.makeText(this, "Clienta: " + clienta.nombre + " (id=" + clienta.id + ")", Toast.LENGTH_SHORT).show();
            }
        });

        cargarClientes(null);
        Utils.setupFooter(this);
    }

    private void cargarClientes(String buscar) {
        tvEstadoClientes.setVisibility(View.VISIBLE);
        tvEstadoClientes.setText(R.string.estado_cargando);
        lvClientes.setVisibility(View.GONE);

        String filtro = (buscar == null || buscar.isEmpty()) ? null : buscar;

        ApiCliente.get(this).obtenerClientes(filtro).enqueue(new Callback<List<Cliente>>() {
            @Override
            public void onResponse(Call<List<Cliente>> call, Response<List<Cliente>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaClientes.clear();
                    listaClientes.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (listaClientes.isEmpty()) {
                        tvEstadoClientes.setText(R.string.estado_sin_clientas);
                    } else {
                        tvEstadoClientes.setVisibility(View.GONE);
                        lvClientes.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEstadoClientes.setText(ApiCliente.mensajeDeError(response));
                }
            }

            @Override
            public void onFailure(Call<List<Cliente>> call, Throwable t) {
                tvEstadoClientes.setText("No se pudo conectar con el servidor");
            }
        });
    }
}

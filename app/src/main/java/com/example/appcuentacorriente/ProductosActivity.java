package com.example.appcuentacorriente;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class ProductosActivity extends AppCompatActivity {
    private RadioGroup rgFiltroStock;
    private TextView tvEstadoProductos;
    private ListView lvProductos;

    private final List<Producto> listaProductos = new ArrayList<>();
    private ArrayAdapter<Producto> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_productos);
        Utils.setupInsetsConPaddingOriginal(this);

        rgFiltroStock = findViewById(R.id.rgFiltroStock);
        tvEstadoProductos = findViewById(R.id.tvEstadoProductos);
        lvProductos = findViewById(R.id.lvProductos);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaProductos);
        lvProductos.setAdapter(adapter);

        rgFiltroStock.setOnCheckedChangeListener((group, checkedId) -> {
            String filtroStock = null;
            if (checkedId == R.id.rbConStock) {
                filtroStock = "con";
            } else if (checkedId == R.id.rbSinStock) {
                filtroStock = "sin";
            }
            cargarProductos(filtroStock);
        });

        cargarProductos(null);
        Utils.setupFooter(this);
    }

    private void cargarProductos(String stock) {
        tvEstadoProductos.setVisibility(View.VISIBLE);
        tvEstadoProductos.setText(R.string.estado_cargando);
        lvProductos.setVisibility(View.GONE);

        ApiCliente.get(this).obtenerProductos(stock).enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaProductos.clear();
                    listaProductos.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (listaProductos.isEmpty()) {
                        tvEstadoProductos.setText(R.string.estado_sin_productos);
                    } else {
                        tvEstadoProductos.setVisibility(View.GONE);
                        lvProductos.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEstadoProductos.setText(ApiCliente.mensajeDeError(response));
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                tvEstadoProductos.setText("No se pudo conectar con el servidor");
            }
        });
    }

}

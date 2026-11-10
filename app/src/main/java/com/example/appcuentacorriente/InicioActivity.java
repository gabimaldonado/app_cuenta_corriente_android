package com.example.appcuentacorriente;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;


public class InicioActivity extends AppCompatActivity {

    private TextView displayDeudaTotal;
    private TextView displayCantDeudores;
    private LinearLayout displayDeudores;
    private TextView displayCantEntregas;
    private LinearLayout displayEntregas;

    private Button btnEntrega;
    private Button btnRegistrarPago;
    private Button btnVerClientes;
    private CuentaCorrienteApi api;
    private Resources res;

    private final LayoutParams paramsVertical = new LayoutParams(
            LayoutParams.MATCH_PARENT,
            0,
            1f
    );
    private final LayoutParams paramsHorizontal = new LayoutParams(
            0,
            LayoutParams.MATCH_PARENT,
            1f
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        displayDeudaTotal = findViewById(R.id.deuda_total);
        displayDeudaTotal.setText(R.string.estado_cargando);
        displayCantDeudores = findViewById(R.id.deuda_total_cant_clientas);
        displayCantDeudores.setText(R.string.estado_cargando);
        displayDeudores = findViewById(R.id.layout_deudores);
        displayCantEntregas = findViewById(R.id.cant_entregas_abiertas);
        displayCantEntregas.setText(R.string.estado_cargando);
        displayEntregas = findViewById(R.id.layout_entregas);

        btnEntrega = findViewById(R.id.btn_nueva_entrega);
        btnRegistrarPago = findViewById(R.id.btn_registrar_pago);
        btnVerClientes = findViewById(R.id.btn_ver_deudores);

        api = ApiCliente.get(this);
        res = getResources();

        hookFuncionesBotones();
        cargarDeudores();
        cargarEntregas();
        footerHandler.setupFooter(this);
    }

    private void cargarDeudores() {
        api.getResumen().enqueue(new Callback<>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<RespuestaResumenDeudores> call, Response<RespuestaResumenDeudores> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                RespuestaResumenDeudores respuesta = response.body();

                displayDeudaTotal.setText(res.getString(R.string.monto_generico,String.valueOf(respuesta.total_deuda_general)));

                int cantDeudores = respuesta.deudores.size();
                String textoDeudores = res.getQuantityString(R.plurals.titulo_cant_deudores,cantDeudores,String.valueOf(cantDeudores));
                displayCantDeudores.setText(textoDeudores);
                for (Deudor deudor: respuesta.deudores) {
                    crearElementoDeudor(deudor,displayDeudores);
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<RespuestaResumenDeudores> call, Throwable throwable) {
                displayDeudaTotal.setText(R.string.err_carga);
                displayCantDeudores.setText(R.string.err_carga);
            }
        });
    }

    private void crearElementoDeudor(Deudor infoDeudor, LinearLayout parent) {
        LinearLayout disp = new LinearLayout(this);
        disp.setOrientation(LinearLayout.HORIZONTAL);
        disp.setLayoutParams(paramsVertical);
        disp.setPadding(0,10,0,0);

        TextView nombre = new TextView(this);
        nombre.setText(infoDeudor.nombre);
        disp.addView(nombre);

        TextView deuda = new TextView(this);
        deuda.setText(res.getString(R.string.monto_generico,String.valueOf(infoDeudor.saldo)));
        deuda.setTextColor(res.getColor(R.color.deuda,null));
        deuda.setTextAlignment(ViewGroup.TEXT_ALIGNMENT_TEXT_END);
        disp.addView(deuda);

        nombre.setTextSize(17);
        deuda.setTextSize(17);
        nombre.setLayoutParams(paramsHorizontal);
        deuda.setLayoutParams(paramsHorizontal);

        parent.addView(disp);
    }

    private void cargarEntregas() {
        api.getEntregas("abierta").enqueue(new Callback<List<Entrega>>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<List<Entrega>> call, Response<List<Entrega>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                List<Entrega> respuesta = response.body();
                int cantEntregas = respuesta.size();
                String textoCantEntregas = res.getQuantityString(R.plurals.cant_entregas_abiertas,cantEntregas,String.valueOf(cantEntregas));
                displayCantEntregas.setText(textoCantEntregas);
                for (Entrega entrega: respuesta) {
                    crearElementoEntrega(entrega,displayEntregas);
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<List<Entrega>> call, Throwable throwable) {
                displayCantEntregas.setText(R.string.err_carga);
            }
        });
    }

    private void crearElementoEntrega(Entrega entrega, LinearLayout parent) {
        Context contextHere = this;
        api.getCliente(entrega.getCliente_id()).enqueue(new Callback<Cliente>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Cliente> call, Response<Cliente> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                Cliente respuesta = response.body();

                //disposicion horizontal.
                // al lado izquierdo ponemos el nombre y los detalles como otro viewgroup, y al derecho el boton de confirmar
                LinearLayout dispHorizontal = new LinearLayout(contextHere);
                dispHorizontal.setOrientation(LinearLayout.HORIZONTAL);
                dispHorizontal.setLayoutParams(paramsVertical);
                dispHorizontal.setPadding(0,10,0,0);

                //disposicion vertical del nombre y los detalles
                LinearLayout dispVertical = new LinearLayout(contextHere);
                dispVertical.setOrientation(LinearLayout.VERTICAL);
                dispVertical.setLayoutParams(paramsHorizontal);
                dispHorizontal.addView(dispVertical);

                TextView nombre = new TextView(contextHere);
                nombre.setText(respuesta.nombre);
                nombre.setLayoutParams(paramsVertical);
                dispVertical.addView(nombre);

                TextView detalles = new TextView(contextHere);
                detalles.setLayoutParams(paramsVertical);
                { //uso de parseo y formato de datetime para determinar el string de los detalles
                    int cantPrendas = entrega.getEntrega_items().size();

                    SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    String fechaFormateada = null;
                    try {
                        Date fecha = parser.parse(entrega.getFecha());
                        assert fecha != null;
                        fechaFormateada = (String) DateUtils.getRelativeTimeSpanString(fecha.getTime());
                    } catch (ParseException e) {
                        fechaFormateada = "Fecha invalida";
                    }
                    String textoDetalle = res.getQuantityString(
                            R.plurals.cant_prendas_entrega,
                            cantPrendas,
                            String.valueOf(cantPrendas),
                            fechaFormateada
                    );

                    detalles.setText(textoDetalle);
                }
                dispVertical.addView(detalles);

                LayoutParams paramsBoton = new LayoutParams(
                        0,
                        LayoutParams.MATCH_PARENT,
                        0.5f
                );
                Button botonConfirmar = new Button(contextHere);
                botonConfirmar.setText(R.string.btn_confirmar_entrega);
                botonConfirmar.setLayoutParams(paramsBoton);
                botonConfirmar.setOnClickListener(v -> {
                    Intent meta = new Intent(InicioActivity.this,ConfirmarEntregaActivity.class);
                    meta.putExtra("idEntrega",entrega.getId());
                    InicioActivity.this.startActivity(meta);
                });
                dispHorizontal.addView(botonConfirmar);


                nombre.setTextSize(17);
                detalles.setTextSize(14);

                parent.addView(dispHorizontal);
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Cliente> call, Throwable throwable) {
                //Uh oh
            }
        });
    }

    private void hookFuncionesBotones() {
        //emergentes, no llaman a "finish"
        btnEntrega.setOnClickListener(v -> abrirPagina(NuevaEntregaActivity.class));
        btnRegistrarPago.setOnClickListener(v -> abrirPagina(InicioActivity.class)); //TODO, FALTA REEMPLAZAR CON LA PANTALLA DE REGISTRO DE PAGO

        //abren otra pagina, llaman a finish
        btnVerClientes.setOnClickListener(v -> footerHandler.abrirPaginaFinish(this,ClientesActivity.class));
    }

    private void abrirPagina(Class<?> pagina) {
        Intent meta = new Intent(InicioActivity.this,pagina);
        InicioActivity.this.startActivity(meta);
    }
}
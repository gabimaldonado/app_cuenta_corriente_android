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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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

    private Button btnSalir;
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
        Utils.setupInsetsConPaddingOriginal(this);

        displayDeudaTotal = findViewById(R.id.deuda_total);
        displayDeudaTotal.setText(R.string.estado_cargando);
        displayCantDeudores = findViewById(R.id.deuda_total_cant_clientas);
        displayCantDeudores.setText(R.string.estado_cargando);
        displayDeudores = findViewById(R.id.layout_deudores);
        displayCantEntregas = findViewById(R.id.cant_entregas_abiertas);
        displayCantEntregas.setText(R.string.estado_cargando);
        displayEntregas = findViewById(R.id.layout_entregas);

        btnSalir = findViewById(R.id.btn_salir);
        btnEntrega = findViewById(R.id.btn_nueva_entrega);
        btnRegistrarPago = findViewById(R.id.btn_registrar_pago);
        btnVerClientes = findViewById(R.id.btn_ver_deudores);

        api = ApiCliente.get(this);
        res = getResources();

        hookFuncionesBotones();
        cargarDeudores();
        cargarEntregas();
    }

    private void cargarDeudores() {
        api.getResumen().enqueue(new Callback<>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<RespuestaResumenDeudores> call, Response<RespuestaResumenDeudores> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    if (response.code() == 401) {
                        Sesion.vencida(InicioActivity.this);
                    } else {
                        String err = ApiCliente.mensajeDeError(response);
                        Toast.makeText(InicioActivity.this,err,Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                RespuestaResumenDeudores respuesta = response.body();

                displayDeudaTotal.setText(getString(R.string.monto_generico,(float) respuesta.total_deuda_general));

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
        disp.setBackground(ResourcesCompat.getDrawable(res,R.drawable.bg_bottom_border,null));

        TextView nombre = new TextView(this);
        nombre.setText(infoDeudor.nombre);
        disp.addView(nombre);

        TextView deuda = new TextView(this);
        deuda.setText(getString(R.string.monto_generico, (float) infoDeudor.saldo));
        deuda.setTextColor(getColor(R.color.deuda));
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
                    if (response.code() == 401) {
                        Sesion.vencida(InicioActivity.this);
                    } else {
                        String err = ApiCliente.mensajeDeError(response);
                        Toast.makeText(InicioActivity.this,err,Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                List<Entrega> entregas = response.body();
                int cantEntregas = entregas.size();
                String textoCantEntregas = res.getQuantityString(R.plurals.cant_entregas_abiertas,cantEntregas,String.valueOf(cantEntregas));
                displayCantEntregas.setText(textoCantEntregas);
                for (Entrega entrega: entregas) {
                    api.getCliente(entrega.getCliente_id()).enqueue(new Callback<Cliente>() {
                        @Override
                        @EverythingIsNonNull
                        public void onResponse(Call<Cliente> call, Response<Cliente> response) {
                            if (!response.isSuccessful() || response.body() == null) {
                                if (response.code() == 401) {
                                    Sesion.vencida(InicioActivity.this);
                                } else {
                                    String err = ApiCliente.mensajeDeError(response);
                                    Toast.makeText(InicioActivity.this,err,Toast.LENGTH_SHORT).show();
                                }
                                return;
                            }
                            Cliente cliente = response.body();
                            crearElementoEntrega(entrega,cliente,displayEntregas);
                        }

                        @Override
                        @EverythingIsNonNull
                        public void onFailure(Call<Cliente> call, Throwable throwable) {
                            crearElementoEntrega(entrega,null,displayEntregas);
                        }
                    });
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<List<Entrega>> call, Throwable throwable) {
                displayCantEntregas.setText(R.string.err_carga);
            }
        });
    }

    private void crearElementoEntrega(Entrega entrega, Cliente cliente, LinearLayout parent) {
        Context contextHere = this;

        //disposicion horizontal.
        // al lado izquierdo ponemos el nombre y los detalles como otro viewgroup, y al derecho el boton de confirmar
        LinearLayout dispHorizontal = new LinearLayout(contextHere);
        dispHorizontal.setOrientation(LinearLayout.HORIZONTAL);
        dispHorizontal.setLayoutParams(paramsVertical);
        dispHorizontal.setPadding(0,10,0,0);
        dispHorizontal.setBackground(ResourcesCompat.getDrawable(res,R.drawable.bg_bottom_border,null));


        //disposicion vertical del nombre y los detalles
        LinearLayout dispVertical = new LinearLayout(contextHere);
        dispVertical.setOrientation(LinearLayout.VERTICAL);
        dispVertical.setLayoutParams(paramsHorizontal);
        dispHorizontal.addView(dispVertical);

        TextView nombre = new TextView(contextHere);
        nombre.setTextSize(17);
        if (cliente != null) {
            nombre.setText(cliente.nombre);
        } else {
            nombre.setText(R.string.err_carga_cliente_entrega);
            nombre.setTextSize(14);
        }
        nombre.setLayoutParams(paramsVertical);
        dispVertical.addView(nombre);

        TextView detalles = new TextView(contextHere);
        detalles.setTextSize(14);
        detalles.setLayoutParams(paramsVertical);
        { //uso de parseo y formato de datetime para determinar el string de los detalles
            int cantPrendas = entrega.getEntrega_items().size();

            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            String fechaFormateada = null;
            try {
                Date fecha = parser.parse(entrega.getFecha());
                assert fecha != null;
                fechaFormateada = (String) DateUtils.getRelativeTimeSpanString(fecha.getTime());
            } catch (ParseException e) {
                fechaFormateada = getString(R.string.fecha_invalida);
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

        parent.addView(dispHorizontal);
    }

    private void hookFuncionesBotones() {
        //emergentes, no llaman a "finish"
        btnEntrega.setOnClickListener(v -> Utils.abrirPagina(this,NuevaEntregaActivity.class));
        //TODO, EN CASO DE QUE REGISTRAR PAGO PUEDA EN ALGUN MOMENTO ABRIRSE SIN EXPLICITAMENTE PASARLE LA CLIENTA, LLAMARLO AQUÍ
        btnRegistrarPago.setOnClickListener(v -> {});

        //abren otra pagina, llaman a finish
        btnVerClientes.setOnClickListener(v -> Utils.abrirPaginaFinish(this,ClientesActivity.class));
        Utils.setupFooter(this);
        btnSalir.setOnClickListener(v -> logout());
    }

    private void logout() {
        Toast.makeText(InicioActivity.this, R.string.msg_cerrando_sesion, Toast.LENGTH_SHORT).show();
        btnSalir.setEnabled(false);
        api.cerrarSesion().enqueue(new Callback<Void>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(InicioActivity.this, R.string.err_cerrar_sesion, Toast.LENGTH_SHORT).show();
                    btnSalir.setEnabled(true);
                    return;
                }
                Sesion.cerrar(InicioActivity.this);
                Toast.makeText(InicioActivity.this, R.string.msg_sesion_cerrada, Toast.LENGTH_SHORT).show();
                Utils.abrirPaginaFinish(InicioActivity.this,LoginActivity.class);
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Void> call, Throwable throwable) {
                Toast.makeText(InicioActivity.this, R.string.err_cerrar_sesion, Toast.LENGTH_SHORT).show();
                btnSalir.setEnabled(true);
            }
        });
    }

}
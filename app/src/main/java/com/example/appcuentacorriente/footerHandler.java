package com.example.appcuentacorriente;

import android.app.Activity;
import android.content.Intent;
import android.widget.Button;

public class footerHandler {
    public static void setupFooter(Activity activity) {
        Button btnFooterInicio = activity.findViewById(R.id.btn_footer_inicio);
        Button btnFooterClientes = activity.findViewById(R.id.btn_footer_clientes);
        Button btnFooterPrendas = activity.findViewById(R.id.btn_footer_prendas);

        btnFooterInicio.setOnClickListener(v -> abrirPaginaFinish(activity,InicioActivity.class));
        btnFooterClientes.setOnClickListener(v -> abrirPaginaFinish(activity,ClientesActivity.class));
        btnFooterPrendas.setOnClickListener(v -> abrirPaginaFinish(activity,ProductosActivity.class));
    }
    
    static void abrirPaginaFinish(Activity activity, Class<?> pagina) {
        Intent meta = new Intent(activity,pagina);
        activity.startActivity(meta);
        activity.finish();
    }
}

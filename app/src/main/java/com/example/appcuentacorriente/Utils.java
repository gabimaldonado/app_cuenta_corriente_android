package com.example.appcuentacorriente;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Utils {
    static void abrirPaginaFinish(Activity activity, Class<?> pagina) {
        Intent meta = new Intent(activity,pagina);
        activity.startActivity(meta);
        activity.finish();
    }

    static void abrirPagina(Activity activity, Class<?> pagina) {
        Intent meta = new Intent(activity,pagina);
        activity.startActivity(meta);
    }

    static void setupInsetsConPaddingOriginal(Activity activity) {
        // La funcion por defecto que se crea en las actividades (setOnApplyWindowInsetsListener)
        //sobrescribe cualquier padding original del elemento main en el layout, ya que se usan estos valores
        //para agregar el inset necesario y que no haya sobreposicion con los elementos del sistema
        // Esta funcion de utilidad guarda el padding original del layout y se lo agrega encima del "inset",
        //para que se conserve
        LinearLayout main = activity.findViewById(R.id.main);
        int paddingLeft,paddingTop,paddingRight,paddingBottom;
        paddingLeft = main.getPaddingLeft();
        paddingTop = main.getPaddingTop();
        paddingRight = main.getPaddingRight();
        paddingBottom = main.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left+paddingLeft, systemBars.top+paddingTop, systemBars.right+paddingRight, systemBars.bottom+paddingBottom);
            return insets;
        });
    }

    public static void setupFooter(Activity activity) {
        Button btnFooterInicio = activity.findViewById(R.id.btn_footer_inicio);
        Button btnFooterClientes = activity.findViewById(R.id.btn_footer_clientes);
        Button btnFooterPrendas = activity.findViewById(R.id.btn_footer_prendas);

        btnFooterInicio.setOnClickListener(v -> abrirPaginaFinish(activity,InicioActivity.class));
        btnFooterClientes.setOnClickListener(v -> abrirPaginaFinish(activity,ClientesActivity.class));
        btnFooterPrendas.setOnClickListener(v -> abrirPaginaFinish(activity,ProductosActivity.class));
    }
}

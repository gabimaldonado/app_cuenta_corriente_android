package com.example.appcuentacorriente;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    // Las vistas de la pantalla (como los querySelector de login.js)
    private EditText etEmail;
    private EditText etPassword;
    private TextView tvMensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // --- Generado por Android Studio: no se toca ---
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Lo nuestro: buscar las vistas por su id ---
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvMensaje = findViewById(R.id.tvMensaje);
        Button btnIngresar = findViewById(R.id.btnIngresar);

        // Qué hacer al tocar el botón (como el addEventListener de login.js)
        btnIngresar.setOnClickListener(v -> ingresar());
    }

    private void ingresar() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        // Validaciones antes de enviar nada (como el 400 de login.php si faltan datos)
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("La contraseña es obligatoria");
            etPassword.requestFocus();
            return;
        }

        // Por ahora solo mostramos que los datos están bien.
        // En la Semana 14 acá se llama a login.php y se guarda el token.
        tvMensaje.setText("Datos válidos para: " + email);
        Toast.makeText(this, "Listo para iniciar sesión", Toast.LENGTH_SHORT).show();
        etPassword.setText("");
    }
}
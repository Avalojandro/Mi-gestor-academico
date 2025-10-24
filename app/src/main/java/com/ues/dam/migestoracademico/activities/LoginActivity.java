package com.ues.dam.migestoracademico.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ues.dam.migestoracademico.R;
import com.ues.dam.migestoracademico.data.AppDB;
import com.ues.dam.migestoracademico.entities.Usuario;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etContrasena;
    private Button btnAcceder, btnRegistrarse;
    private AppDB db;
    private CheckBox cbGuardarSesion;

    private static final String PREF_SESION = "SesionApp";
    private static final String CLAVE_SESION_ACTIVA = "sesionActiva";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarVistas();
        db = AppDB.getInstance(this);

        btnAcceder.setOnClickListener(v -> iniciarSesion());
        btnRegistrarse.setOnClickListener(v -> irARegistro());
    }

    private void inicializarVistas() {
        etEmail = findViewById(R.id.etEmail);
        etContrasena = findViewById(R.id.etContrasena);
        btnAcceder = findViewById(R.id.btnAcceder);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        cbGuardarSesion = findViewById(R.id.guardarSesion);
    }

    private void iniciarSesion() {
        String email = etEmail.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        if (email.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Usuario usuarioEncontrado = db.usuarioDAO().login(email, contrasena);

                runOnUiThread(() -> {
                    if (usuarioEncontrado != null) {
                        //anadiendo logica para solo guardar la session cuando el checkbox esta activo
                        if (cbGuardarSesion.isChecked()){
                            guardarSesionActiva();
                        }else {
                            Toast.makeText(this, "No se va guardar la session", Toast.LENGTH_SHORT).show();

                        }


                        Intent intent = new Intent(LoginActivity.this, SplashActivityAccess.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this, "Error en el inicio de sesión", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void guardarSesionActiva() {
        SharedPreferences preferencias = getSharedPreferences(PREF_SESION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putBoolean(CLAVE_SESION_ACTIVA, true);
        editor.apply();
    }

    public static boolean sesionActiva(Context contexto) {
        SharedPreferences preferencias = contexto.getSharedPreferences(PREF_SESION, Context.MODE_PRIVATE);
        return preferencias.getBoolean(CLAVE_SESION_ACTIVA, false);
    }

    public static void cerrarSesion(Context contexto) {
        SharedPreferences preferencias = contexto.getSharedPreferences(PREF_SESION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.clear();
        editor.apply();
    }

    private void irARegistro() {
        startActivity(new Intent(this, RegisterActivity.class));
        finish();
    }
}
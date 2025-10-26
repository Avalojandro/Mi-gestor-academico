package com.ues.dam.migestoracademico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
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
import com.ues.dam.migestoracademico.repositories.UsuarioRepository;

import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etContrasena, etNombre;
    private Button btnRegistrarse, btnIniciarSesion;
    private AppDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarVistas();
        db = AppDB.getInstance(this);

        btnRegistrarse.setOnClickListener(v -> registrarUsuario());
        btnIniciarSesion.setOnClickListener(v -> irALogin());
    }

    private void inicializarVistas() {
        etEmail = findViewById(R.id.etEmail);
        etContrasena = findViewById(R.id.etContrasena);
        etNombre = findViewById(R.id.etNombre);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        btnIniciarSesion = findViewById(R.id.btnAcceder);
    }

    private void registrarUsuario() {
        String email = etEmail.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();
        String name = etNombre.getText().toString().trim();

        // Validaciones
        if (name.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            etNombre.requestFocus();
            return;
        }
        if (!isValidName(name)) {
            etNombre.setError("El nombre solo puede tener letras y espacios");
            etNombre.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("El email es obligatorio");
            etEmail.requestFocus();
            return;
        }
        if (!isValidEmail(email)) {
            etEmail.setError("Formato de email inválido");
            etEmail.requestFocus();
            return;
        }

        if (contrasena.isEmpty()) {
            etContrasena.setError("La contraseña es obligatoria");
            etContrasena.requestFocus();
            return;
        }
        if (!isValidPassword(contrasena)) {
            etContrasena.setError("Mín. 8 caracteres, alfanumérica (al menos 1 letra y 1 número)");
            etContrasena.requestFocus();
            return;
        }

        // Formatos validos, siguiente paso
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                int existe = db.usuarioDAO().existeUsuario(email);

                if (existe > 0) {
                    runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this, "El email ya existe", Toast.LENGTH_SHORT).show());
                    return;
                }

                Usuario nuevoUsuario = new Usuario(email, contrasena, name);
                UsuarioRepository.saveUser(new Usuario(email, contrasena, name));
                db.usuarioDAO().crear(nuevoUsuario);

                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, "Error al registrar email", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String pwd) {
        return pwd.length() >= 8 && pwd.matches("^(?=.*[A-Za-z])(?=.*\\d).+$");
    }

    private boolean isValidName(String name) {
        return name.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü\\s]{2,}$");
    }

    private void irALogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
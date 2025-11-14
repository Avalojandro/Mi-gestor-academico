package com.ues.dam.migestoracademico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etContrasena, etNombre;
    private Button btnRegistrarse, btnIniciarSesion;
    private AppDB db;
    private boolean isPasswordVisible = false;

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

        etUsuario = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        etEmail = findViewById(R.id.etEmail);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        btnIniciarSesion = findViewById(R.id.btnAcceder);

        ImageView togglePasswordVisibilityImageView = findViewById(R.id.togglePasswordVisibilityImageView);
        db = AppDB.getInstance(this);

        btnRegistrarse.setOnClickListener(v -> registrarUsuario());
        btnIniciarSesion.setOnClickListener(v -> irALogin());

        togglePasswordVisibilityImageView.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etContrasena.setTransformationMethod(PasswordTransformationMethod.getInstance());
                togglePasswordVisibilityImageView.setImageResource(R.drawable.ic_eye_closed);
            } else {
                etContrasena.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                togglePasswordVisibilityImageView.setImageResource(R.drawable.ic_eye_open);
            }
            etContrasena.setSelection(etContrasena.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });
    }

    private void inicializarVistas() {
        etEmail= findViewById(R.id.etEmail);
        etContrasena = findViewById(R.id.etContrasena);
        etNombre = findViewById(R.id.etNombre);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        btnIniciarSesion = findViewById(R.id.btnAcceder);
    }

    private void registrarUsuario() {
        String email = etEmail.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();
        String name = etNombre.getText().toString().trim();


        if (email.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (contrasena.length() < 8) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!contrasena.matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            Toast.makeText(this, "La contraseña debe contener al menos una letra y un número", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                int existe = db.usuarioDAO().existeUsuario(email);

                if (existe > 0) {
                    runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this, "El email ya existe", Toast.LENGTH_SHORT).show());
                    return;
                }

                Usuario nuevoUsuario = new Usuario(email, contrasena, name );
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

    private void irALogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
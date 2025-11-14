package com.ues.dam.migestoracademico.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.QuerySnapshot;
import com.ues.dam.migestoracademico.R;
import com.ues.dam.migestoracademico.data.AppDB;
import com.ues.dam.migestoracademico.entities.Usuario;
import com.ues.dam.migestoracademico.repositories.UsuarioRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etContrasena;
    private Button btnAcceder, btnRegistrarse;
    private CheckBox cbGuardarSesion;
    private AppDB db;
    private CheckBox cbGuardarSesion;
    private boolean isPasswordVisible = false;

    private static final String PREF_SESION = "SesionApp";
    private static final String PREF_PERFIL = "perfil";
    private static final String CLAVE_SESION_ACTIVA = "sesionActiva";
    private static final String CLAVE_EMAIL = "emailUsuario";
    private static final String CLAVE_DOC_ID = "docIdUsuario";
    private static final String CLAVE_ROOM_ID = "roomUsuarioId";

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

        etUsuario = findViewById(R.id.etUsuario);
        etContrasena = findViewById(R.id.etContrasena);
        btnAcceder = findViewById(R.id.btnAcceder);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        cbGuardarSesion = findViewById(R.id.guardarSesion);

        ImageView togglePasswordVisibilityImageView = findViewById(R.id.togglePasswordVisibilityImageView);
        db = AppDB.getInstance(this);

        btnAcceder.setOnClickListener(v -> iniciarSesion());
        btnRegistrarse.setOnClickListener(v -> irARegistro());

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
        etEmail = findViewById(R.id.etEmail);
        etContrasena = findViewById(R.id.etContrasena);
        btnAcceder = findViewById(R.id.btnAcceder);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        cbGuardarSesion = findViewById(R.id.guardarSesion);
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error en el hashing de contraseña", e);
        }
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
                AtomicReference<Usuario> usr = new AtomicReference<>();

                AtomicReference<String> docId = new AtomicReference<>();
//--------------------------------------------------------
                UsuarioRepository.getUserByEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                QuerySnapshot snapshot = task.getResult();

                                Usuario usuarioEncontradoFirestore = null;
                                String documentId = null;

                                if (snapshot != null && !snapshot.isEmpty() ) {
                                    usuarioEncontradoFirestore = snapshot.getDocuments().get(0).toObject(Usuario.class);
                                    //agregado obtener el id del usauario 
                                    documentId = snapshot.getDocuments().get(0).getId();
                                    usr.set(usuarioEncontradoFirestore);
                                    docId.set(documentId);
                                }
                            } else {
                                Log.e("ERROR", "Error al buscar usuario", task.getException());
                            }
                           //--
                            runOnUiThread(() -> {
                                if (usuarioEncontrado != null && usr.get() != null && docId.get() != null) {
                                    guardarPerfilDeUsuario(email, docId.get(), usuarioEncontrado.id);
                                    if (cbGuardarSesion.isChecked()){
                                        guardarSesionActiva(email, docId.get());
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
                        });
                //-------------------------


                Usuario usuarioEncontrado = db.usuarioDAO().buscarPorUsername(usuario);

                boolean loginExitoso = false;
                if (usuarioEncontrado != null) {
                    String contrasenaHashIngresada = hashPassword(contrasena);
                    loginExitoso = usuarioEncontrado.password.equals(contrasenaHashIngresada);
                }

                final boolean finalLoginExitoso = loginExitoso;
                runOnUiThread(() -> {
                    if (finalLoginExitoso) {
                        if (cbGuardarSesion.isChecked()) {
                            guardarSesionActiva();
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
                        Toast.makeText(LoginActivity.this, "Error en el inicio de sesión: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void guardarSesionActiva(String email, String docId) {
        SharedPreferences preferencias = getSharedPreferences(PREF_SESION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putBoolean(CLAVE_SESION_ACTIVA, true);
        editor.putString(CLAVE_EMAIL, email);
        editor.putString(CLAVE_DOC_ID, docId);
        editor.apply();
    }

    private void guardarPerfilDeUsuario(String email, String docId, int roomId) {
        SharedPreferences preferencias = getSharedPreferences(PREF_PERFIL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putBoolean(CLAVE_SESION_ACTIVA, true);
        editor.putString(CLAVE_EMAIL, email);
        editor.putString(CLAVE_DOC_ID, docId);
        editor.putInt(CLAVE_ROOM_ID, roomId);
        editor.apply();
    }

    public static boolean sesionActiva(Context context) {
        SharedPreferences preferencias = context.getSharedPreferences(PREF_SESION, Context.MODE_PRIVATE);
        return preferencias.getBoolean(CLAVE_SESION_ACTIVA, false);
    }

    public static void cerrarSesion(Context context) {
        SharedPreferences preferencias = context.getSharedPreferences(PREF_SESION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.clear();
        editor.apply();
        SharedPreferences preferenciasPerfil = contexto.getSharedPreferences(PREF_PERFIL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorPerfil = preferenciasPerfil.edit();
        editorPerfil.clear();
        editorPerfil.apply();
    }

    private void irARegistro() {
        startActivity(new Intent(this, RegisterActivity.class));
        finish();
    }
}
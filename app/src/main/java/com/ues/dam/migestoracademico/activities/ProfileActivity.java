package com.ues.dam.migestoracademico.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ues.dam.migestoracademico.R;
import com.ues.dam.migestoracademico.data.AppDB;
import com.ues.dam.migestoracademico.entities.Usuario;

import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail;
    private Button btnEditProfile;
    private AppDB db;
    private Usuario currentUser;
    private String docId;

    private static final String PREF_SESION = "SesionApp";
    private static final String CLAVE_EMAIL = "emailUsuario";
    private static final String CLAVE_DOC_ID = "docIdUsuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        db = AppDB.getInstance(this);

        loadUserProfile();

        btnEditProfile.setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("CURRENT_NAME", currentUser.getName());
                intent.putExtra("CURRENT_EMAIL", currentUser.getEmail());
                intent.putExtra("DOC_ID", docId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No se pudo cargar el usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user profile in case it was edited
        loadUserProfile();
    }

    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences(PREF_SESION, Context.MODE_PRIVATE);
        String email = prefs.getString(CLAVE_EMAIL, null);
        docId = prefs.getString(CLAVE_DOC_ID, null);

        if (email != null) {
            Executors.newSingleThreadExecutor().execute(() -> {
                // We read from the local Room DB (our 'cache')
                currentUser = db.usuarioDAO().buscarPorEmail(email);

                runOnUiThread(() -> {
                    if (currentUser != null) {
                        tvProfileName.setText(currentUser.getName());
                        tvProfileEmail.setText(currentUser.getEmail());
                    }
                });
            });
        }else {

            Toast.makeText(this, "No se pudo cargar el usuario", Toast.LENGTH_SHORT).show();
        }
    }
}
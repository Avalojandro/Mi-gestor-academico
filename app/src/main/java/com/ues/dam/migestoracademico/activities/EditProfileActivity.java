package com.ues.dam.migestoracademico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ues.dam.migestoracademico.R;
import com.ues.dam.migestoracademico.data.AppDB;
import com.ues.dam.migestoracademico.entities.Usuario;
import com.ues.dam.migestoracademico.repositories.UsuarioRepository;

import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etEditName;
    private TextView tvEditEmail;
    private Button btnSaveProfile;

    private AppDB db;
    private String currentEmail, docId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etEditName = findViewById(R.id.etEditName);
        tvEditEmail = findViewById(R.id.tvEditEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        db = AppDB.getInstance(this);

        // Get data from ProfileActivity
        Intent intent = getIntent();
        String currentName = intent.getStringExtra("CURRENT_NAME");
        currentEmail = intent.getStringExtra("CURRENT_EMAIL");
        docId = intent.getStringExtra("DOC_ID");

        // Populate fields
        etEditName.setText(currentName);
        tvEditEmail.setText(currentEmail);

        btnSaveProfile.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String newName = etEditName.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Get the full user object from Room
            Usuario usuario = db.usuarioDAO().buscarPorEmail(currentEmail);
            if (usuario == null) {
                runOnUiThread(() -> Toast.makeText(this, "Error: No se encontró el usuario local", Toast.LENGTH_SHORT).show());
                return;
            }

            // 2. Update the name
            usuario.name = newName;

            // 3. Save to local Room DB
            db.usuarioDAO().actualizar(usuario);

            // 4. Save to remote Firestore DB
            UsuarioRepository.updateUser(docId, usuario)
                    .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to ProfileActivity
                    }))
                    .addOnFailureListener(e -> runOnUiThread(() -> {
                        Toast.makeText(this, "Error al actualizar en Firestore", Toast.LENGTH_SHORT).show();
                    }));
        });
    }
}
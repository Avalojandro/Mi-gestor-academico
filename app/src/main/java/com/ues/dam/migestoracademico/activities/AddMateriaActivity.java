package com.ues.dam.migestoracademico.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ues.dam.migestoracademico.R;
import com.ues.dam.migestoracademico.data.AppDB;
import com.ues.dam.migestoracademico.entities.Materia;
import com.ues.dam.migestoracademico.repositories.MateriaRepository;

import java.util.concurrent.Executors;

public class AddMateriaActivity extends AppCompatActivity {

    private EditText etMateriaNombre, etMateriaCodigo, etMateriaUV;
    private Button btnGuardarMateria;
    private AppDB db;

    // Constantes para SharedPreferences
    private static final String PREF_PERFIL = "perfil";
    private static final String CLAVE_DOC_ID = "docIdUsuario";
    private static final String CLAVE_ROOM_ID = "roomUsuarioId";

    private String userDocId;
    private int userRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_materia);

        db = AppDB.getInstance(this);

        etMateriaNombre = findViewById(R.id.etMateriaNombre);
        etMateriaCodigo = findViewById(R.id.etMateriaCodigo);
        etMateriaUV = findViewById(R.id.etMateriaUV);
        btnGuardarMateria = findViewById(R.id.btnGuardarMateria);

        // Cargar los IDs del usuario desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREF_PERFIL, Context.MODE_PRIVATE);
        userDocId = prefs.getString(CLAVE_DOC_ID, null);
        userRoomId = prefs.getInt(CLAVE_ROOM_ID, -1);

        btnGuardarMateria.setOnClickListener(v -> guardarMateria());
    }

    private void guardarMateria() {
        String nombre = etMateriaNombre.getText().toString().trim();
        String codigo = etMateriaCodigo.getText().toString().trim();
        String uvString = etMateriaUV.getText().toString().trim();

        if (nombre.isEmpty() || codigo.isEmpty() || uvString.isEmpty() || userDocId == null || userRoomId == -1) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int uv;
        try {
            uv = Integer.parseInt(uvString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Las UVs deben ser un número", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Crear el objeto Materia
        Materia nuevaMateria = new Materia(nombre, codigo, uv, null, userDocId, userRoomId);

        // 2. Guardar en Firestore PRIMERO para obtener el ID
        MateriaRepository.crear(nuevaMateria)
                .addOnSuccessListener(documentReference -> {
                    // 3. Obtener el ID de Firestore y asignarlo al objeto
                    String firestoreId = documentReference.getId();
                    nuevaMateria.firestoreId = firestoreId;

                    // 4. Guardar en la base de datos local (Room)
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.materiaDAO().crear(nuevaMateria);

                        // 5. Volver al MainActivity
                        runOnUiThread(() -> {
                            Toast.makeText(AddMateriaActivity.this, "Materia guardada", Toast.LENGTH_SHORT).show();
                            finish(); // Cierra esta actividad y regresa a MainActivity
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> Toast.makeText(AddMateriaActivity.this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show());
                });
    }
}
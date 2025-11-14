package com.ues.dam.migestoracademico.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; //
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ues.dam.migestoracademico.R;
import com.ues.dam.migestoracademico.data.AppDB;
import com.ues.dam.migestoracademico.entities.Materia;
import com.ues.dam.migestoracademico.repositories.MateriaRepository;

import java.util.Locale; //
import java.util.concurrent.Executors;

public class AddEditMateriaActivity extends AppCompatActivity {

    private EditText etMateriaNombre, etMateriaCodigo, etMateriaUV;
    private Button btnGuardarMateria;
    private TextView tvTituloForm; // AÑADIR
    private AppDB db;

    // ... (constantes de SharedPreferences) ...
    private static final String PREF_PERFIL = "perfil";
    private static final String CLAVE_DOC_ID = "docIdUsuario";
    private static final String CLAVE_ROOM_ID = "roomUsuarioId";

    private String userDocId;
    private int userRoomId;

    // --- AÑADIR ESTAS VARIABLES ---
    private boolean isEditMode = false;
    private Materia materiaActual;
    private int materiaId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_materia);

        db = AppDB.getInstance(this);

        // --- Inicializar vistas ---
        tvTituloForm = findViewById(R.id.tvTituloForm);
        etMateriaNombre = findViewById(R.id.etMateriaNombre);
        etMateriaCodigo = findViewById(R.id.etMateriaCodigo);
        etMateriaUV = findViewById(R.id.etMateriaUV);
        btnGuardarMateria = findViewById(R.id.btnGuardarMateria);

        // ... (Cargar IDs de usuario desde SharedPreferences) ...
        SharedPreferences prefs = getSharedPreferences(PREF_PERFIL, Context.MODE_PRIVATE);
        userDocId = prefs.getString(CLAVE_DOC_ID, null);
        userRoomId = prefs.getInt(CLAVE_ROOM_ID, -1);

        // --- LÓGICA DE EDICIÓN ---
        // Comprobar si recibimos un ID de materia
        materiaId = getIntent().getIntExtra("MATERIA_ID", -1);

        if (materiaId != -1) {
            isEditMode = true;
            // Estamos en Modo Edición
            tvTituloForm.setText("Editar Materia");
            btnGuardarMateria.setText("Actualizar Cambios");
            loadMateriaData(materiaId);
        } else {
            // Estamos en Modo Creación (como estaba antes)
            isEditMode = false;
            tvTituloForm.setText("Nueva Materia");
            btnGuardarMateria.setText("Guardar Materia");
        }
        // --- FIN LÓGICA DE EDICIÓN ---

        btnGuardarMateria.setOnClickListener(v -> guardarMateria());
    }

    private void loadMateriaData(int id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Obtener la materia de la BD local
            materiaActual = db.materiaDAO().obtenerPorId(id);

            // 2. Poblar los campos en el hilo principal
            runOnUiThread(() -> {
                if (materiaActual != null) {
                    etMateriaNombre.setText(materiaActual.nombre);
                    etMateriaCodigo.setText(materiaActual.codigo);
                    etMateriaUV.setText(String.format(Locale.getDefault(), "%d", materiaActual.uv));
                } else {
                    // Error: No se encontró la materia
                    Toast.makeText(this, "Error al cargar la materia", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void guardarMateria() {
        // --- Validación (igual que antes) ---
        String nombre = etMateriaNombre.getText().toString().trim();
        String codigo = etMateriaCodigo.getText().toString().trim();
        String uvString = etMateriaUV.getText().toString().trim();

        if (nombre.isEmpty() || codigo.isEmpty() || uvString.isEmpty()) {
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

        // --- LÓGICA DE GUARDADO/ACTUALIZACIÓN ---
        if (isEditMode) {
            // --- MODO EDICIÓN: ACTUALIZAR ---
            // 1. Actualizar el objeto 'materiaActual'
            materiaActual.nombre = nombre;
            materiaActual.codigo = codigo;
            materiaActual.uv = uv;

            Executors.newSingleThreadExecutor().execute(() -> {
                // 2. Actualizar en Room
                db.materiaDAO().actualizar(materiaActual);

                // 3. Actualizar en Firestore
                if (materiaActual.firestoreId != null) {
                    MateriaRepository.actualizar(materiaActual.firestoreId, materiaActual)
                            .addOnFailureListener(e ->
                                    runOnUiThread(() -> Toast.makeText(AddEditMateriaActivity.this, "Error al actualizar en Firestore", Toast.LENGTH_SHORT).show()));
                }

                // 4. Finalizar
                runOnUiThread(() -> {
                    Toast.makeText(this, "Materia actualizada", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });

        } else {
            // --- MODO CREACIÓN: (Tu lógica anterior) ---
            if (userDocId == null || userRoomId == -1) {
                Toast.makeText(this, "Error de sesión de usuario", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Crear el objeto Materia
            Materia nuevaMateria = new Materia(nombre, codigo, uv, null, userDocId, userRoomId);

            // 2. Guardar en Firestore PRIMERO
            MateriaRepository.crear(nuevaMateria)
                    .addOnSuccessListener(documentReference -> {
                        String firestoreId = documentReference.getId();
                        nuevaMateria.firestoreId = firestoreId;

                        // 4. Guardar en Room
                        Executors.newSingleThreadExecutor().execute(() -> {
                            db.materiaDAO().crear(nuevaMateria);

                            // 5. Volver
                            runOnUiThread(() -> {
                                Toast.makeText(AddEditMateriaActivity.this, "Materia guardada", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        });
                    })
                    .addOnFailureListener(e -> {
                        runOnUiThread(() -> Toast.makeText(AddEditMateriaActivity.this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show());
                    });
        }
    }
}
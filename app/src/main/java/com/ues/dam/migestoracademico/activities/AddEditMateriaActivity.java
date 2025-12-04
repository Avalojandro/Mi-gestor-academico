package com.ues.dam.migestoracademico.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ues.dam.migestoracademico.R;
import com.ues.dam.migestoracademico.data.AppDB;
import com.ues.dam.migestoracademico.entities.Materia;
import com.ues.dam.migestoracademico.repositories.MateriaRepository;

import java.util.Locale;
import java.util.concurrent.Executors;

public class AddEditMateriaActivity extends AppCompatActivity {

    private EditText etMateriaNombre, etMateriaCodigo, etMateriaUV;
    private Button btnGuardarMateria;
    private TextView tvTituloForm;
    private AppDB db;

    private static final String PREF_PERFIL = "perfil";
    private static final String CLAVE_DOC_ID = "docIdUsuario";
    private static final String CLAVE_ROOM_ID = "roomUsuarioId";

    private String userDocId;
    private int userRoomId;

    private boolean isEditMode = false;
    private Materia materiaActual;
    private int materiaId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_materia);

        db = AppDB.getInstance(this);


        tvTituloForm = findViewById(R.id.tvTituloForm);
        etMateriaNombre = findViewById(R.id.etMateriaNombre);
        etMateriaCodigo = findViewById(R.id.etMateriaCodigo);
        etMateriaUV = findViewById(R.id.etMateriaUV);
        btnGuardarMateria = findViewById(R.id.btnGuardarMateria);

        SharedPreferences prefs = getSharedPreferences(PREF_PERFIL, Context.MODE_PRIVATE);
        userDocId = prefs.getString(CLAVE_DOC_ID, null);
        userRoomId = prefs.getInt(CLAVE_ROOM_ID, -1);


        materiaId = getIntent().getIntExtra("MATERIA_ID", -1);

        if (materiaId != -1) {
            isEditMode = true;
            tvTituloForm.setText("Editar Materia");
            btnGuardarMateria.setText("Actualizar Cambios");
            loadMateriaData(materiaId);
        } else {

            isEditMode = false;
            tvTituloForm.setText("Nueva Materia");
            btnGuardarMateria.setText("Guardar Materia");
        }


        btnGuardarMateria.setOnClickListener(v -> guardarMateria());
    }

    private void loadMateriaData(int id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            materiaActual = db.materiaDAO().obtenerPorId(id);

            runOnUiThread(() -> {
                if (materiaActual != null) {
                    etMateriaNombre.setText(materiaActual.nombre);
                    etMateriaCodigo.setText(materiaActual.codigo);
                    etMateriaUV.setText(String.format(Locale.getDefault(), "%d", materiaActual.uv));
                } else {
                    Toast.makeText(this, "Error al cargar la materia", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void guardarMateria() {
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


        if (isEditMode) {

            materiaActual.nombre = nombre;
            materiaActual.codigo = codigo;
            materiaActual.uv = uv;

            Executors.newSingleThreadExecutor().execute(() -> {

                db.materiaDAO().actualizar(materiaActual);


                if (materiaActual.firestoreId != null) {
                    MateriaRepository.actualizar(materiaActual.firestoreId, materiaActual)
                            .addOnFailureListener(
                                    e -> runOnUiThread(
                                            () -> Toast
                                                    .makeText(AddEditMateriaActivity.this,
                                                            "Error al actualizar en Firestore", Toast.LENGTH_SHORT)
                                                    .show()));
                }


                runOnUiThread(() -> {
                    Toast.makeText(this, "Materia actualizada", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });

        } else {

            if (userDocId == null || userRoomId == -1) {
                Toast.makeText(this, "Error de sesión de usuario", Toast.LENGTH_SHORT).show();
                return;
            }


            Materia nuevaMateria = new Materia(nombre, codigo, uv, null, userDocId, userRoomId);


            MateriaRepository.crear(nuevaMateria)
                    .addOnSuccessListener(documentReference -> {
                        String firestoreId = documentReference.getId();
                        nuevaMateria.firestoreId = firestoreId;

                        Executors.newSingleThreadExecutor().execute(() -> {
                            db.materiaDAO().crear(nuevaMateria);

                            runOnUiThread(() -> {
                                Toast.makeText(AddEditMateriaActivity.this, "Materia guardada", Toast.LENGTH_SHORT)
                                        .show();
                                finish();
                            });
                        });
                    })
                    .addOnFailureListener(e -> {
                        runOnUiThread(() -> Toast.makeText(AddEditMateriaActivity.this, "Error al guardar en Firestore",
                                Toast.LENGTH_SHORT).show());
                    });
        }
    }
}

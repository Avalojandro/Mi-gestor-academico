package com.ues.dam.migestoracademico.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ues.dam.migestoracademico.R;
import com.ues.dam.migestoracademico.data.AppDB;
import com.ues.dam.migestoracademico.entities.Actividad;
import com.ues.dam.migestoracademico.repositories.ActividadRepository;

import java.util.List;
import java.util.concurrent.Executors;

public class ActividadesActivity extends AppCompatActivity implements ActividadAdapter.OnActividadListener {

    private TextView tvTituloMateria;
    private RecyclerView rvActividades;
    private FloatingActionButton fabAddActividad;

    private AppDB db;
    private ActividadAdapter adapter;

    private int materiaId;
    private String materiaNombre;
    private String materiaFirestoreId;

    // VARIABLE PARA EVITAR DUPLICADOS POR DOBLE CARGA
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividades);

        db = AppDB.getInstance(this);

        tvTituloMateria = findViewById(R.id.tvTituloMateria);
        rvActividades = findViewById(R.id.rvActividades);
        fabAddActividad = findViewById(R.id.fabAddActividad);

        rvActividades.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActividadAdapter(this);
        rvActividades.setAdapter(adapter);

        if (getIntent().hasExtra("MATERIA_ID")) {
            materiaId = getIntent().getIntExtra("MATERIA_ID", -1);
            materiaNombre = getIntent().getStringExtra("MATERIA_NOMBRE");
            materiaFirestoreId = getIntent().getStringExtra("MATERIA_FS_ID");

            if (materiaNombre != null) tvTituloMateria.setText(materiaNombre);
        } else {
            Toast.makeText(this, "Error al cargar la materia", Toast.LENGTH_SHORT).show();
            finish();
        }

        fabAddActividad.setOnClickListener(v -> {
            Intent intent = new Intent(ActividadesActivity.this, AddEditActividadActivity.class);
            intent.putExtra("MATERIA_ID", materiaId);
            intent.putExtra("MATERIA_FS_ID", materiaFirestoreId);
            startActivity(intent);
        });

        // --- CORRECCIÓN 1: ELIMINAMOS loadActividades() DE AQUÍ ---
        // loadActividades(); <--- ESTO CAUSABA EL DUPLICADO AL CHOCAR CON ONRESUME
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ESTO ES SUFICIENTE: Se ejecuta al iniciar Y al volver de agregar
        loadActividades();
    }

    private void loadActividades() {
        // --- CORRECCIÓN 2: EVITAR EJECUCIÓN SIMULTÁNEA ---
        if (isLoading) return;
        isLoading = true;

        // 1. CARGA RÁPIDA (Local)
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Actividad> lista = db.actividadDAO().obtenerPorMateria(materiaId);
            runOnUiThread(() -> {
                if (!lista.isEmpty()) adapter.setActividades(lista);
            });
        });

        // 2. SINCRONIZACIÓN (Nube)
        if (materiaFirestoreId != null) {
            ActividadRepository.obtenerPorMateria(materiaFirestoreId).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Actividad> listaNube = task.getResult().toObjects(Actividad.class);

                    for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                        listaNube.get(i).firestoreId = task.getResult().getDocuments().get(i).getId();
                        listaNube.get(i).materiaId = materiaId;
                    }

                    Executors.newSingleThreadExecutor().execute(() -> {
                        // Limpiamos y reinsertamos
                        db.actividadDAO().eliminarTodasDeMateria(materiaId);
                        db.actividadDAO().insertarTodas(listaNube);

                        List<Actividad> actualizada = db.actividadDAO().obtenerPorMateria(materiaId);
                        runOnUiThread(() -> {
                            adapter.setActividades(actualizada);
                            isLoading = false; // LIBERAMOS EL BLOQUEO
                        });
                    });
                } else {
                    isLoading = false; // LIBERAMOS SI FALLA
                    Toast.makeText(this, "Error al sincronizar", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            isLoading = false; // LIBERAMOS SI NO HAY ID
        }
    }


    @Override
    public void onEditClick(Actividad actividad) {
        Intent intent = new Intent(this, AddEditActividadActivity.class);
        // Pasamos los IDs de la materia (contexto)
        intent.putExtra("MATERIA_ID", materiaId);
        intent.putExtra("MATERIA_FS_ID", materiaFirestoreId);

        // Pasamos el objeto actividad completo para editarlo
        intent.putExtra("ACTIVIDAD_OBJ", actividad);

        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Actividad actividad, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Actividad")
                .setMessage("¿Deseas eliminar " + actividad.nombre + "?")
                .setPositiveButton("Sí", (dialog, which) -> borrarActividad(actividad, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void borrarActividad(Actividad actividad, int position) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.actividadDAO().eliminar(actividad);

            if (actividad.firestoreId != null) {
                ActividadRepository.eliminar(actividad.firestoreId);
            }

            runOnUiThread(() -> {
                adapter.removerActividad(position);
                Toast.makeText(this, "Actividad eliminada", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
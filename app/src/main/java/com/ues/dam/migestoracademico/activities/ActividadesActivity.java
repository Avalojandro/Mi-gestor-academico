package com.ues.dam.migestoracademico.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.ues.dam.migestoracademico.entities.Materia;
import com.ues.dam.migestoracademico.repositories.ActividadRepository;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ActividadesActivity extends AppCompatActivity implements ActividadAdapter.OnActividadListener {

    private TextView tvHeaderNombre, tvHeaderUV, tvHeaderPromedio;
    private TextView tvEmptyState;

    private RecyclerView rvActividades;
    private FloatingActionButton fabAddActividad;

    private AppDB db;
    private ActividadAdapter adapter;

    private int materiaId;
    private String materiaFirestoreId;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividades);

        db = AppDB.getInstance(this);

        tvHeaderNombre = findViewById(R.id.tvHeaderNombreMateria);
        tvHeaderUV = findViewById(R.id.tvHeaderUV);
        tvHeaderPromedio = findViewById(R.id.tvHeaderPromedio);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvActividades = findViewById(R.id.rvActividades);
        fabAddActividad = findViewById(R.id.fabAddActividad);

        rvActividades.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActividadAdapter(this);
        rvActividades.setAdapter(adapter);

        if (getIntent().hasExtra("MATERIA_ID")) {
            materiaId = getIntent().getIntExtra("MATERIA_ID", -1);
            materiaFirestoreId = getIntent().getStringExtra("MATERIA_FS_ID");

            String nombreTemp = getIntent().getStringExtra("MATERIA_NOMBRE");
            if(nombreTemp != null) tvHeaderNombre.setText(nombreTemp);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActividades();
    }

    private void loadActividades() {
        if (isLoading) return;
        isLoading = true;

        Executors.newSingleThreadExecutor().execute(() -> {

            Materia materiaObj = db.materiaDAO().obtenerPorId(materiaId);

            List<Actividad> lista = db.actividadDAO().obtenerPorMateria(materiaId);

            double sumaPromedio = 0;
            for (Actividad a : lista) {
                sumaPromedio += (a.nota * (a.porcentaje / 100.0));
            }
            final double promedioFinal = sumaPromedio;

            runOnUiThread(() -> {
                adapter.setActividades(lista);
                checkEmptyState();

                if (materiaObj != null) {
                    tvHeaderNombre.setText(materiaObj.nombre);
                    tvHeaderUV.setText(materiaObj.uv + " UV");
                }

                actualizarBadgePromedio(promedioFinal);
            });
        });

        if (materiaFirestoreId != null) {
            ActividadRepository.obtenerPorMateria(materiaFirestoreId).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Actividad> listaNube = task.getResult().toObjects(Actividad.class);
                    for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                        listaNube.get(i).firestoreId = task.getResult().getDocuments().get(i).getId();
                        listaNube.get(i).materiaId = materiaId;
                    }

                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.actividadDAO().eliminarTodasDeMateria(materiaId);
                        db.actividadDAO().insertarTodas(listaNube);

                        List<Actividad> actualizada = db.actividadDAO().obtenerPorMateria(materiaId);
                        Materia matObj = db.materiaDAO().obtenerPorId(materiaId);

                        double suma = 0;
                        for (Actividad a : actualizada) {
                            suma += (a.nota * (a.porcentaje / 100.0));
                        }
                        final double promFinal = suma;

                        runOnUiThread(() -> {
                            adapter.setActividades(actualizada);
                            checkEmptyState();
                            if (matObj != null) {
                                tvHeaderNombre.setText(matObj.nombre);
                                tvHeaderUV.setText(matObj.uv + " UV");
                            }
                            actualizarBadgePromedio(promFinal);
                            isLoading = false;
                        });
                    });
                } else {
                    isLoading = false;
                }
            });
        } else {
            isLoading = false;
        }
    }

    private void checkEmptyState() {
        if (adapter.getItemCount() == 0) {
            tvEmptyState.setVisibility(android.view.View.VISIBLE);
        } else {
            tvEmptyState.setVisibility(android.view.View.GONE);
        }
    }

    private void actualizarBadgePromedio(double promedio) {
        tvHeaderPromedio.setText(String.format(Locale.getDefault(), "%.1f", promedio));

        int colorFondo;
        if (promedio < 6.0) {
            colorFondo = Color.parseColor("#E53935");
        } else if (promedio < 8.0) {
            colorFondo = Color.parseColor("#FB8C00");
        } else {
            colorFondo = Color.parseColor("#43A047");
        }
        tvHeaderPromedio.setBackgroundTintList(ColorStateList.valueOf(colorFondo));
    }

    @Override
    public void onEditClick(Actividad actividad) {
        Intent intent = new Intent(this, AddEditActividadActivity.class);
        intent.putExtra("MATERIA_ID", materiaId);
        intent.putExtra("MATERIA_FS_ID", materiaFirestoreId);
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
                checkEmptyState();
                Toast.makeText(this, "Actividad eliminada", Toast.LENGTH_SHORT).show();
                isLoading = false;
                loadActividades();
            });
        });
    }
}
package com.ues.dam.migestoracademico.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ues.dam.migestoracademico.R;
import com.ues.dam.migestoracademico.data.AppDB;
import com.ues.dam.migestoracademico.entities.Materia;
import com.ues.dam.migestoracademico.repositories.MateriaRepository;

import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements MateriaAdapter.OnMateriaListener {

    private AppDB db;
    private RecyclerView rvMaterias;
    private MateriaAdapter materiaAdapter;
    private FloatingActionButton fabAddMateria;
    private FloatingActionButton fabMap; // ← botón del mapa

    // Constantes para SharedPreferences
    private static final String PREF_PERFIL = "perfil";
    private static final String CLAVE_EMAIL = "emailUsuario";
    private static final String CLAVE_ROOM_ID = "roomUsuarioId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializar DB y vistas
        db = AppDB.getInstance(this);
        rvMaterias = findViewById(R.id.rvMaterias);
        fabAddMateria = findViewById(R.id.fabAddMateria);
        fabMap = findViewById(R.id.fabMap);

        // Configurar RecyclerView
        rvMaterias.setLayoutManager(new LinearLayoutManager(this));
        materiaAdapter = new MateriaAdapter(this);
        rvMaterias.setAdapter(materiaAdapter);

        // Manejar insets del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Agregar materia
        fabAddMateria.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddEditMateriaActivity.class))
        );

        // Abrir el mapa
        fabMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        // Cargar las materias al iniciar
        loadMaterias();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar materias al volver al home
        loadMaterias();
    }

    private void loadMaterias() {
        SharedPreferences prefs = getSharedPreferences(PREF_PERFIL, Context.MODE_PRIVATE);
        int userRoomId = prefs.getInt(CLAVE_ROOM_ID, -1);

        if (userRoomId == -1) {
            Toast.makeText(this, "Error al identificar al usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Materia> materias = db.materiaDAO().obtenerPorUsuario(userRoomId);

            runOnUiThread(() -> materiaAdapter.setMaterias(materias));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            cerrarSesion();
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEditClick(Materia materia) {
        Intent intent = new Intent(MainActivity.this, AddEditMateriaActivity.class);
        intent.putExtra("MATERIA_ID", materia.id);
        startActivity(intent);
    }

    private void cerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    LoginActivity.cerrarSesion(this);
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDeleteClick(Materia materia, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Borrado")
                .setMessage("¿Estás seguro de que quieres eliminar la materia '" + materia.nombre + "'?")
                .setPositiveButton("Sí, Eliminar", (dialog, which) -> borrarMateria(materia, position))
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void borrarMateria(Materia materia, int position) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.materiaDAO().eliminar(materia);

            if (materia.firestoreId != null && !materia.firestoreId.isEmpty()) {
                MateriaRepository.eliminar(materia.firestoreId)
                        .addOnFailureListener(e ->
                                Log.e("FirestoreDelete", "Error al borrar materia de Firestore", e)
                        );
            }

            runOnUiThread(() -> {
                materiaAdapter.removerMateria(position);
                Toast.makeText(this, "Materia eliminada", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
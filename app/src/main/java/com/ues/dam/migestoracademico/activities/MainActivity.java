package com.ues.dam.migestoracademico.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.ues.dam.migestoracademico.data.AppDB; // IMPORTAR
import com.ues.dam.migestoracademico.entities.Materia;

import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private AppDB db;
    private RecyclerView rvMaterias; // AÑADIR
    private MateriaAdapter materiaAdapter; // AÑADIR
    private FloatingActionButton fabAddMateria;


    //const para sharedprefs
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

        // Inicializar DB y Vistas
        db = AppDB.getInstance(this);
        rvMaterias = findViewById(R.id.rvMaterias);
        fabAddMateria = findViewById(R.id.fabAddMateria);

        // Configurar RecyclerView
        rvMaterias.setLayoutManager(new LinearLayoutManager(this));
        materiaAdapter = new MateriaAdapter();
        rvMaterias.setAdapter(materiaAdapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fabAddMateria.setOnClickListener(v -> {
            // Aún no hemos creado esta actividad, pero así es como la llamaríamos
            // startActivity(new Intent(MainActivity.this, AddEditMateriaActivity.class));
            Toast.makeText(this, "Funcionalidad de añadir pendiente", Toast.LENGTH_SHORT).show();
        });

        // Cargar las materias
        loadMaterias();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar materias cada vez que volvemos a esta pantalla
        loadMaterias();
    }


    private void loadMaterias() {
        // Obtener el ID de Room del usuario desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREF_PERFIL, Context.MODE_PRIVATE);
        int userRoomId = prefs.getInt(CLAVE_ROOM_ID, -1);

        if (userRoomId == -1) {
            // Esto no debería pasar si el usuario está logueado
            Toast.makeText(this, "Error: No se pudo identificar al usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ejecutar en un hilo separado
        Executors.newSingleThreadExecutor().execute(() -> {
            // Obtener materias de la BD local (Room)
            List<Materia> materias = db.materiaDAO().obtenerPorUsuario(userRoomId);

            // Actualizar el UI en el hilo principal
            runOnUiThread(() -> {
                materiaAdapter.setMaterias(materias);
            });
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
        }else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cerrarSesion() {
        new android.app.AlertDialog.Builder(this)
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
}
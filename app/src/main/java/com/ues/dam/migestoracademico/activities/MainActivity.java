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

import com.google.firebase.auth.FirebaseAuth;
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
    private FloatingActionButton fabMap;
    private boolean isLoading = false;

    // Constantes para SharedPreferences
    private static final String PREF_PERFIL = "perfil";
    private static final String CLAVE_ROOM_ID = "roomUsuarioId";
    private static final String CLAVE_DOC_ID = "docIdUsuario";

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
        // Pasamos 'this' porque MainActivity implementa la interfaz OnMateriaListener
        materiaAdapter = new MateriaAdapter(this);
        rvMaterias.setAdapter(materiaAdapter);

        // Manejar insets del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Agregar materia
        fabAddMateria.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddEditMateriaActivity.class)));

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
        loadMaterias();
    }

    private void loadMaterias() {
        if (isLoading) return;
        isLoading = true;

        SharedPreferences prefs = getSharedPreferences(PREF_PERFIL, Context.MODE_PRIVATE);
        String userDocId = prefs.getString(CLAVE_DOC_ID, null);
        int userRoomId = prefs.getInt(CLAVE_ROOM_ID, -1);

        if (userDocId == null || userRoomId == -1) {
            isLoading = false;
            return;
        }

        // 1. CARGA LOCAL (Rápida)
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Materia> materiasLocales = db.materiaDAO().obtenerPorUsuario(userRoomId);

            // Calculamos promedio local
            calcularPromedios(materiasLocales);

            runOnUiThread(() -> materiaAdapter.setMaterias(materiasLocales));

            // 2. SINCRONIZACIÓN CON LA NUBE (Corrección)
            MateriaRepository.obtenerPorUsuario(userDocId).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Materia> materiasCloud = task.getResult().toObjects(Materia.class);

                    Executors.newSingleThreadExecutor().execute(() -> {

                        // RECORREMOS LO QUE VINO DE LA NUBE
                        for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                            Materia mCloud = materiasCloud.get(i);
                            String fsId = task.getResult().getDocuments().get(i).getId();
                            mCloud.setFirestoreId(fsId);

                            // ¿Ya existe esta materia en mi celular?
                            Materia mLocal = db.materiaDAO().obtenerPorFirestoreId(fsId);

                            if (mLocal != null) {
                                // SI EXISTE: Actualizamos solo los textos, PERO MANTENEMOS EL ID LOCAL
                                // Esto evita que se borren las actividades
                                mLocal.nombre = mCloud.nombre;
                                mLocal.codigo = mCloud.codigo;
                                mLocal.uv = mCloud.uv;
                                db.materiaDAO().actualizar(mLocal);
                            } else {
                                // NO EXISTE: La creamos
                                mCloud.setUserId(userRoomId);
                                db.materiaDAO().insertar(mCloud);
                            }
                        }

                        // Opcional: Aquí podrías borrar las materias locales que ya no están en la nube
                        // pero por seguridad dejémoslo así por ahora.

                        // 3. RECARGAR Y RECALCULAR FINALMENTE
                        List<Materia> materiasFinales = db.materiaDAO().obtenerPorUsuario(userRoomId);
                        calcularPromedios(materiasFinales);

                        runOnUiThread(() -> {
                            materiaAdapter.setMaterias(materiasFinales);
                            isLoading = false;
                        });
                    });
                } else {
                    isLoading = false;
                }
            });
        });
    }

    // Método auxiliar para no repetir código del cálculo
    private void calcularPromedios(List<Materia> materias) {
        for (Materia m : materias) {
            List<com.ues.dam.migestoracademico.entities.Actividad> acts = db.actividadDAO().obtenerPorMateria(m.id);
            double suma = 0;
            for (com.ues.dam.migestoracademico.entities.Actividad a : acts) {
                // Cálculo: Nota * (Porcentaje / 100)
                suma += (a.nota * (a.porcentaje / 100.0));
            }
            m.promedioCalculado = suma;
        }
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

    // --- IMPLEMENTACIÓN DE INTERFAZ OnMateriaListener ---

    // 1. EDITAR (Click en botón lápiz)
    @Override
    public void onEditClick(Materia materia) {
        Intent intent = new Intent(MainActivity.this, AddEditMateriaActivity.class);
        intent.putExtra("MATERIA_ID", materia.id);
        startActivity(intent);
    }

    // 2. CLICK EN TARJETA (Click en el fondo -> Ver Actividades) - NUEVO
    @Override
    public void onMateriaClick(Materia materia) {
        Intent intent = new Intent(MainActivity.this, ActividadesActivity.class);
        intent.putExtra("MATERIA_ID", materia.id);
        intent.putExtra("MATERIA_NOMBRE", materia.nombre);
        intent.putExtra("MATERIA_FS_ID", materia.firestoreId);
        startActivity(intent);
    }

    // 3. BORRAR (Click en botón basura)
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

    // ----------------------------------------------------

    private void cerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    LoginActivity.cerrarSesion(this);
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void borrarMateria(Materia materia, int position) {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.materiaDAO().eliminar(materia);

            if (materia.firestoreId != null && !materia.firestoreId.isEmpty()) {
                MateriaRepository.eliminar(materia.firestoreId)
                        .addOnFailureListener(e -> Log.e("FirestoreDelete", "Error al borrar materia de Firestore", e));
            }

            runOnUiThread(() -> {
                materiaAdapter.removerMateria(position);
                Toast.makeText(this, "Materia eliminada", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
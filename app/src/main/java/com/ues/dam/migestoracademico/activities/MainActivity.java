package com.ues.dam.migestoracademico.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
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
    private boolean isLoading = false;
    private TextView tvNombreUsuario;

    private static final String PREF_PERFIL = "perfil";
    private static final String CLAVE_ROOM_ID = "roomUsuarioId";
    private static final String CLAVE_DOC_ID = "docIdUsuario";
    private static final String CLAVE_EMAIL = "emailUsuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = AppDB.getInstance(this);
        rvMaterias = findViewById(R.id.rvMaterias);
        fabAddMateria = findViewById(R.id.fabAddMateria);

        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);

        rvMaterias.setLayoutManager(new LinearLayoutManager(this));
        materiaAdapter = new MateriaAdapter(this);
        rvMaterias.setAdapter(materiaAdapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fabAddMateria.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddEditMateriaActivity.class)));

        loadMaterias();
        mostrarNombreUsuario();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMaterias();
        mostrarNombreUsuario();
    }

    private void mostrarNombreUsuario() {
        Executors.newSingleThreadExecutor().execute(() -> {
            SharedPreferences prefs = getSharedPreferences(PREF_PERFIL, Context.MODE_PRIVATE);
            String email = prefs.getString(CLAVE_EMAIL, null);

            if (email != null) {
                com.ues.dam.migestoracademico.entities.Usuario usuario = db.usuarioDAO().buscarPorEmail(email);
                runOnUiThread(() -> {
                    if (usuario != null && usuario.name != null) {
                        tvNombreUsuario.setText(usuario.name);
                    } else {
                        tvNombreUsuario.setText("Usuario");
                    }
                });
            }
        });
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

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Materia> materiasLocales = db.materiaDAO().obtenerPorUsuario(userRoomId);

            calcularPromedios(materiasLocales);

            runOnUiThread(() -> materiaAdapter.setMaterias(materiasLocales));

            MateriaRepository.obtenerPorUsuario(userDocId).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Materia> materiasCloud = task.getResult().toObjects(Materia.class);

                    Executors.newSingleThreadExecutor().execute(() -> {

                        for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                            Materia mCloud = materiasCloud.get(i);
                            String fsId = task.getResult().getDocuments().get(i).getId();
                            mCloud.setFirestoreId(fsId);

                            Materia mLocal = db.materiaDAO().obtenerPorFirestoreId(fsId);

                            if (mLocal != null) {
                                mLocal.nombre = mCloud.nombre;
                                mLocal.codigo = mCloud.codigo;
                                mLocal.uv = mCloud.uv;
                                db.materiaDAO().actualizar(mLocal);
                            } else {
                                mCloud.setUserId(userRoomId);
                                db.materiaDAO().insertar(mCloud);
                            }
                        }

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

    private void calcularPromedios(List<Materia> materias) {
        for (Materia m : materias) {
            List<com.ues.dam.migestoracademico.entities.Actividad> acts = db.actividadDAO().obtenerPorMateria(m.id);
            double suma = 0;
            for (com.ues.dam.migestoracademico.entities.Actividad a : acts) {
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

    @Override
    public void onEditClick(Materia materia) {
        Intent intent = new Intent(MainActivity.this, AddEditMateriaActivity.class);
        intent.putExtra("MATERIA_ID", materia.id);
        startActivity(intent);
    }

    @Override
    public void onMateriaClick(Materia materia) {
        Intent intent = new Intent(MainActivity.this, ActividadesActivity.class);
        intent.putExtra("MATERIA_ID", materia.id);
        intent.putExtra("MATERIA_NOMBRE", materia.nombre);
        intent.putExtra("MATERIA_FS_ID", materia.firestoreId);
        startActivity(intent);
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
package com.ues.dam.migestoracademico.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ues.dam.migestoracademico.R;
import com.ues.dam.migestoracademico.data.AppDB;
import com.ues.dam.migestoracademico.entities.Actividad;
import com.ues.dam.migestoracademico.repositories.ActividadRepository;

import java.util.concurrent.Executors;

public class AddEditActividadActivity extends AppCompatActivity {

    private EditText etNombre, etDescripcion, etFecha, etPorcentaje;
    private Button btnGuardar;
    private TextView tvTitulo; // Para cambiar el título "Nueva Actividad" a "Editar Actividad"

    private AppDB db;

    private int materiaId;
    private String materiaFirestoreId;

    // Variable para saber si estamos editando
    private Actividad actividadEditar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_actividad);

        db = AppDB.getInstance(this);

        // Vincular vistas
        tvTitulo = findViewById(R.id.tvTituloHeader); // Asegúrate de agregar ID al TextView del título en el XML si quieres cambiarlo, o usa findViewByClass
        // Si no tienes ID en el título, ignora la línea de arriba o agrégale: android:id="@+id/tvTituloHeader" al XML

        etNombre = findViewById(R.id.etNombreActividad);
        etDescripcion = findViewById(R.id.etDescripcionActividad);
        etFecha = findViewById(R.id.etFechaActividad);
        etPorcentaje = findViewById(R.id.etPorcentajeActividad);
        btnGuardar = findViewById(R.id.btnGuardarActividad);

        // 1. Obtener datos de la Materia (Contexto)
        if (getIntent().hasExtra("MATERIA_ID")) {
            materiaId = getIntent().getIntExtra("MATERIA_ID", -1);
            materiaFirestoreId = getIntent().getStringExtra("MATERIA_FS_ID");
        }

        // 2. VERIFICAR SI ESTAMOS EN MODO EDICIÓN
        if (getIntent().hasExtra("ACTIVIDAD_OBJ")) {
            // Recuperamos el objeto
            actividadEditar = (Actividad) getIntent().getSerializableExtra("ACTIVIDAD_OBJ");

            // Llenamos los campos
            if (actividadEditar != null) {
                etNombre.setText(actividadEditar.nombre);
                etDescripcion.setText(actividadEditar.descripcion);
                etFecha.setText(actividadEditar.fecha);
                etPorcentaje.setText(String.valueOf(actividadEditar.porcentaje));

                // Cambiamos el texto del botón
                btnGuardar.setText("Actualizar Actividad");
            }
        }

        btnGuardar.setOnClickListener(v -> guardarActividad());
    }

    private void guardarActividad() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String porcentajeStr = etPorcentaje.getText().toString().trim();

        if (nombre.isEmpty() || porcentajeStr.isEmpty()) {
            Toast.makeText(this, "Nombre y Porcentaje son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double porcentaje = Double.parseDouble(porcentajeStr);

        // DEFINIR EL OBJETO A GUARDAR
        Actividad actividadFinal;

        if (actividadEditar != null) {
            // --- MODO EDICIÓN: Usamos el objeto existente y actualizamos sus campos ---
            actividadEditar.nombre = nombre;
            actividadEditar.descripcion = descripcion;
            actividadEditar.fecha = fecha;
            actividadEditar.porcentaje = porcentaje;

            // Mantenemos los IDs que ya tenía (id local y firestoreId)
            actividadFinal = actividadEditar;
        } else {
            // --- MODO CREACIÓN: Creamos uno nuevo ---
            actividadFinal = new Actividad(nombre, descripcion, fecha, porcentaje, materiaId, materiaFirestoreId);
        }

        btnGuardar.setEnabled(false); // Bloquear botón

        // 1. Guardar/Actualizar en Firebase
        ActividadRepository.guardar(actividadFinal).addOnSuccessListener(unused -> {

            // 2. Guardar/Actualizar en Local (Room)
            Executors.newSingleThreadExecutor().execute(() -> {
                // Room es inteligente: si el objeto tiene un ID que ya existe, @Insert(REPLACE) lo actualiza.
                db.actividadDAO().insertar(actividadFinal);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Actividad guardada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });

        }).addOnFailureListener(e -> {
            btnGuardar.setEnabled(true);
            Toast.makeText(this, "Error al guardar en la nube: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
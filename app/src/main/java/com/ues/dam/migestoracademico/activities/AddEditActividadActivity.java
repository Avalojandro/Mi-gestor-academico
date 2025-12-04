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

    private EditText etNombre, etDescripcion, etFecha, etPorcentaje, etNota;
    private Button btnGuardar;
    private TextView tvTitulo;

    private AppDB db;

    private int materiaId;
    private String materiaFirestoreId;
    private Actividad actividadEditar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_actividad);

        db = AppDB.getInstance(this);

        tvTitulo = findViewById(R.id.tvTituloHeader);
        etNombre = findViewById(R.id.etNombreActividad);
        etDescripcion = findViewById(R.id.etDescripcionActividad);
        etFecha = findViewById(R.id.etFechaActividad);
        etPorcentaje = findViewById(R.id.etPorcentajeActividad);
        etNota = findViewById(R.id.etNotaActividad);

        btnGuardar = findViewById(R.id.btnGuardarActividad);

        if (getIntent().hasExtra("MATERIA_ID")) {
            materiaId = getIntent().getIntExtra("MATERIA_ID", -1);
            materiaFirestoreId = getIntent().getStringExtra("MATERIA_FS_ID");
        }

        if (getIntent().hasExtra("ACTIVIDAD_OBJ")) {
            actividadEditar = (Actividad) getIntent().getSerializableExtra("ACTIVIDAD_OBJ");

            if (actividadEditar != null) {
                if (tvTitulo != null) tvTitulo.setText("Editar Actividad");

                etNombre.setText(actividadEditar.nombre);
                etDescripcion.setText(actividadEditar.descripcion);
                etFecha.setText(actividadEditar.fecha);
                etPorcentaje.setText(String.valueOf(actividadEditar.porcentaje));

                etNota.setText(String.valueOf(actividadEditar.nota));

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
        String notaStr = etNota.getText().toString().trim();

        if (nombre.isEmpty() || porcentajeStr.isEmpty()) {
            Toast.makeText(this, "Nombre y Porcentaje son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double porcentaje = Double.parseDouble(porcentajeStr);

        double nota = 0.0;
        if (!notaStr.isEmpty()) {
            nota = Double.parseDouble(notaStr);
        }

        Actividad actividadFinal;

        if (actividadEditar != null) {
            actividadEditar.nombre = nombre;
            actividadEditar.descripcion = descripcion;
            actividadEditar.fecha = fecha;
            actividadEditar.porcentaje = porcentaje;
            actividadEditar.nota = nota;

            actividadFinal = actividadEditar;
        } else {
            actividadFinal = new Actividad(nombre, descripcion, fecha, porcentaje, materiaId, materiaFirestoreId);
            actividadFinal.nota = nota;
        }

        btnGuardar.setEnabled(false);

        ActividadRepository.guardar(actividadFinal).addOnSuccessListener(unused -> {

            Executors.newSingleThreadExecutor().execute(() -> {
                db.actividadDAO().insertar(actividadFinal);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Actividad guardada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });

        }).addOnFailureListener(e -> {
            btnGuardar.setEnabled(true);
            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
package com.ues.dam.migestoracademico.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.ues.dam.migestoracademico.R;

public class AddSubjectActivity extends AppCompatActivity {

    private TextInputEditText etName, etCode, etUv;
    private Button btnSave, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subject);

        // Configurar toolbar con botón "Atrás"
        Toolbar toolbar = findViewById(R.id.toolbarAdd);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Agregar Materia");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar vistas
        etName = findViewById(R.id.etName);
        etCode = findViewById(R.id.etCode);
        etUv   = findViewById(R.id.etUv);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Acción del botón Guardar
        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String code = etCode.getText() != null ? etCode.getText().toString().trim() : "";
            String uv   = etUv.getText() != null ? etUv.getText().toString().trim() : "";

            if (name.isEmpty() || code.isEmpty() || uv.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Aquí podrías guardar en BD o enviar a MainActivity
            Toast.makeText(this, "Materia guardada correctamente", Toast.LENGTH_SHORT).show();

            // Finalizar actividad (regresa atrás)
            finish();
        });

        // Acción del botón Cancelar
        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Manejar clic en el botón "Atrás" de la Toolbar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // ✅ versión compatible
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
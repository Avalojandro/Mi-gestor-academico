package com.ues.dam.migestoracademico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.ues.dam.migestoracademico.R;

public class SplashActivity extends AppCompatActivity {

    private static final int RETRASO_SPLASH = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // Habilitar la persistencia de Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        db.setFirestoreSettings(settings);

        // Forzar el intento de reconexion y sincronizacion
        db.enableNetwork();

        new Handler().postDelayed(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null && LoginActivity.sesionActiva(this)) {
                // Usuario ya esta logueado y la sesión esta guardada
                startActivity(new Intent(this, SplashActivityAccess.class));
            } else {
                // Si no hay usuario logueado o no se guardó la sesión, cerrar sesión de
                // Firebase e ir al login
                FirebaseAuth.getInstance().signOut();
                LoginActivity.cerrarSesion(this); // Limpiar SharedPreferences
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, RETRASO_SPLASH);
    }
}

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
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        // Forzar el intento de reconexion y sincronizacion
        db.enableNetwork();

        new Handler().postDelayed(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null && LoginActivity.sesionActiva(this)) {
                // usuario ya esta logueado y la sesion esta guardada
                startActivity(new Intent(this, SplashActivityAccess.class));
            } else {
                // s i no hay usuario logueado o no se guardo la sesion, cerrar sesion de
                // firebase y ir al login
                FirebaseAuth.getInstance().signOut();
                LoginActivity.cerrarSesion(this); // Limpiar SharedPreferences
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, RETRASO_SPLASH);
    }
}

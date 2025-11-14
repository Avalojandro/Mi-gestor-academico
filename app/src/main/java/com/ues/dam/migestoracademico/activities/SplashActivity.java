package com.ues.dam.migestoracademico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.ues.dam.migestoracademico.R;

public class SplashActivity extends AppCompatActivity {

    private static final int RETRASO_SPLASH = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            if (LoginActivity.sesionActiva(this)) {
                // Usuario ya está logueado, ir al dashboard
                startActivity(new Intent(this, SplashActivityAccess.class));
            } else {
                // Usuario no logueado, ir al login
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, RETRASO_SPLASH);
    }
}
package com.ues.dam.migestoracademico.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.ues.dam.migestoracademico.R;

public class SplashActivityAccess extends AppCompatActivity {

    private static final int ACCESS_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_access);

        new Handler().postDelayed(() -> {
            // Ir al dashboard principal
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, ACCESS_DELAY);
    }
}
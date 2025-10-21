package com.ues.dam.migestoracademico.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.ues.dam.migestoracademico.dao.UsuarioDAO;
import com.ues.dam.migestoracademico.entities.Usuario;

@Database(entities = {Usuario.class}, version = 1)
public abstract class AppDB extends RoomDatabase {
    private static AppDB instancia;
    public abstract UsuarioDAO usuarioDAO();

    public static synchronized AppDB getInstance(Context context) {
        if (instancia == null) {
            instancia = Room.databaseBuilder(context.getApplicationContext(),
                            AppDB.class, "db_gestor_academico")
                    .build();
        }
        return instancia;
    }
}
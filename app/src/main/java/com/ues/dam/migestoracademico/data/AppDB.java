package com.ues.dam.migestoracademico.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

// Importar los DAOs existentes
import com.ues.dam.migestoracademico.dao.MateriaDAO;
import com.ues.dam.migestoracademico.dao.UsuarioDAO;

// --- CORRECCIÓN AQUÍ ---
// Antes tenías: .data.ActividadDAO
// Debe ser:     .dao.ActividadDAO
import com.ues.dam.migestoracademico.dao.ActividadDAO;

// Importar las entidades
import com.ues.dam.migestoracademico.entities.Materia;
import com.ues.dam.migestoracademico.entities.Usuario;
import com.ues.dam.migestoracademico.entities.Actividad;

@Database(entities = {Usuario.class, Materia.class, Actividad.class}, version = 2)
public abstract class AppDB extends RoomDatabase {

    private static AppDB instancia;

    public abstract UsuarioDAO usuarioDAO();
    public abstract MateriaDAO materiaDAO();
    public abstract ActividadDAO actividadDAO();

    public static synchronized AppDB getInstance(Context context) {
        if (instancia == null) {
            instancia = Room.databaseBuilder(context.getApplicationContext(),
                            AppDB.class, "db_gestor_academico")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instancia;
    }
}
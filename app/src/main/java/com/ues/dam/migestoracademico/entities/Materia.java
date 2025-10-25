package com.ues.dam.migestoracademico.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

@Entity(tableName = "materias", indices = {@Index(value = "user_id")})
public class Materia {

    // ID local para Room.
    // @Exclude para que Firestore no lo guarde.
    @PrimaryKey(autoGenerate = true)
    @Exclude
    public int id;

    // Campos de la materia
    public String nombre;
    public String codigo;
    public int uv;

    // ID del usuario en Firestore (para la nube)
    public String userDocId;

    // ID local del usuario en Room (para la BD local)
    // @Exclude para que Firestore no lo guarde.
    @ColumnInfo(name = "user_id")
    @Exclude
    public int userId;

    // Constructor vacío requerido por Firestore
    public Materia() {
    }

    // Constructor principal
    public Materia(String nombre, String codigo, int uv, String userDocId, int userId) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.uv = uv;
        this.userDocId = userDocId;
        this.userId = userId;
    }

    // --- Getters y Setters ---
    // (Puedes generarlos automáticamente en Android Studio)
    // (Asegúrate de poner @Exclude en los getters/setters de 'id' y 'userId' si los creas)

    @Exclude
    public int getId() {
        return id;
    }
}
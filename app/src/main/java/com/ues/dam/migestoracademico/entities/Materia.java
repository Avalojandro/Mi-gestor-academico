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
    public String firestoreId;

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

    public Materia( String nombre, String codigo, int uv, String firestoreId, String userDocId, int userId) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.uv = uv;
        this.firestoreId = firestoreId;
        this.userDocId = userDocId;
        this.userId = userId;
    }

    // --- Getters y Setters ---
    // (Puedes generarlos automáticamente en Android Studio)
    // (Asegúrate de poner @Exclude en los getters/setters de 'id' y 'userId' si los creas)


    @Exclude
    public int getUserId() {
        return userId;
    }

    @Exclude
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserDocId() {
        return userDocId;
    }

    public void setUserDocId(String userDocId) {
        this.userDocId = userDocId;
    }

    public String getFirestoreId() {
        return firestoreId;
    }

    public void setFirestoreId(String firestoreId) {
        this.firestoreId = firestoreId;
    }

    public int getUv() {
        return uv;
    }

    public void setUv(int uv) {
        this.uv = uv;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Exclude
    public void setId(int id) {
        this.id = id;
    }

    @Exclude
    public int getId() {
        return id;
    }
}
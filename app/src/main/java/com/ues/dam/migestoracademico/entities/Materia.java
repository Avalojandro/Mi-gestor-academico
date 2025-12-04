package com.ues.dam.migestoracademico.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

@Entity(tableName = "materias", indices = { @Index(value = "user_id") })
public class Materia implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @Exclude
    public int id;

    public String nombre;
    public String codigo;
    public int uv;
    public String firestoreId;
    public String userDocId;

    @ColumnInfo(name = "user_id")
    @Exclude
    public int userId;

    // @Ignore: Room no lo guarda en la base de datos local
    // @Exclude: Firestore no lo sube a la nube
    @Ignore
    @Exclude
    public double promedioCalculado = 0.0;

    public Materia() {
    }

    public Materia(String nombre, String codigo, int uv, String firestoreId, String userDocId, int userId) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.uv = uv;
        this.firestoreId = firestoreId;
        this.userDocId = userDocId;
        this.userId = userId;
    }

    @Exclude
    public int getUserId() { return userId; }
    @Exclude
    public void setUserId(int userId) { this.userId = userId; }
    public String getUserDocId() { return userDocId; }
    public void setUserDocId(String userDocId) { this.userDocId = userDocId; }
    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }
    public int getUv() { return uv; }
    public void setUv(int uv) { this.uv = uv; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    @Exclude
    public void setId(int id) { this.id = id; }
    @Exclude
    public int getId() { return id; }
}
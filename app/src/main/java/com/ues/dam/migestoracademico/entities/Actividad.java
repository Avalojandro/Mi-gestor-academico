package com.ues.dam.migestoracademico.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

@Entity(tableName = "actividades",
        foreignKeys = @ForeignKey(
                entity = Materia.class,
                parentColumns = "id",
                childColumns = "materiaId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("materiaId")}
)
public class Actividad implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;


    public String nombre;
    public String descripcion;
    public String fecha;
    public double nota;
    public double porcentaje;


    public int materiaId;

    public String firestoreId;
    public String materiaFirestoreId;


    public Actividad() {}


    public Actividad(String nombre, String descripcion, String fecha, double porcentaje, int materiaId, String materiaFirestoreId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.porcentaje = porcentaje;
        this.materiaId = materiaId;
        this.materiaFirestoreId = materiaFirestoreId;
        this.nota = 0.0;
    }


    @Exclude
    public int getId() { return id; }
}
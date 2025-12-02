package com.ues.dam.migestoracademico.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

// Definimos la clave foránea: Si borras una Materia, se borran sus Actividades (CASCADE)
@Entity(tableName = "actividades",
        foreignKeys = @ForeignKey(
                entity = Materia.class,
                parentColumns = "id",
                childColumns = "materiaId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("materiaId")} // Índice para hacer las consultas rápidas
)
public class Actividad implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id; // ID Local (Room)

    // Datos de la actividad
    public String nombre;
    public String descripcion;
    public String fecha; // Guardaremos la fecha como texto por simplicidad (ej: "12/10/2023")
    public double nota;  // Ejemplo: 8.5
    public double porcentaje; // Ejemplo: 20%

    // Relaciones
    public int materiaId; // Relación Local (Foreign Key)

    public String firestoreId; // ID del documento en Firebase
    public String materiaFirestoreId; // ID de la materia en Firebase

    // Constructor vacío requerido por Firebase
    public Actividad() {}

    // Constructor para crear nuevas actividades fácilmente
    public Actividad(String nombre, String descripcion, String fecha, double porcentaje, int materiaId, String materiaFirestoreId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.porcentaje = porcentaje;
        this.materiaId = materiaId;
        this.materiaFirestoreId = materiaFirestoreId;
        this.nota = 0.0; // Nota inicial 0
    }

    // Getters necesarios para Firebase (o usa public fields como arriba)
    @Exclude // Excluimos el ID local de enviarse a Firebase
    public int getId() { return id; }
}
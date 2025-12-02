package com.ues.dam.migestoracademico.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy; // Importante
import androidx.room.Query;
import androidx.room.Update;

import com.ues.dam.migestoracademico.entities.Materia;

import java.util.List;

@Dao
public interface MateriaDAO {

    @Insert
    void crear(Materia materia);

    // Agregamos este para que coincida con el código de MainActivity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Materia materia);

    @Insert
    void crearTodas(List<Materia> materias);

    @Update
    void actualizar(Materia materia);

    @Delete
    void eliminar(Materia materia);

    @Query("DELETE FROM materias WHERE user_id = :userId")
    void eliminarPorUsuario(int userId);

    @Query("SELECT * FROM materias WHERE user_id = :userId")
    List<Materia> obtenerPorUsuario(int userId);

    @Query("SELECT * FROM materias WHERE id = :materiaId")
    Materia obtenerPorId(int materiaId);

    // --- NUEVO: NECESARIO PARA EVITAR EL BORRADO DE ACTIVIDADES ---
    @Query("SELECT * FROM materias WHERE firestoreId = :firestoreId LIMIT 1")
    Materia obtenerPorFirestoreId(String firestoreId);
}
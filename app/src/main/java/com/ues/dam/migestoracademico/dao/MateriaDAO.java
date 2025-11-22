package com.ues.dam.migestoracademico.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.ues.dam.migestoracademico.entities.Materia;

import java.util.List;

@Dao
public interface MateriaDAO {

    @Insert
    void crear(Materia materia);

    @Insert
    void crearTodas(List<Materia> materias);

    @Update
    void actualizar(Materia materia);

    @Delete
    void eliminar(Materia materia);

    @Query("DELETE FROM materias WHERE user_id = :userId")
    void eliminarPorUsuario(int userId);

    // Query para obtener todas las materias de un usuario específico (usando el ID local de Room)
    @Query("SELECT * FROM materias WHERE user_id = :userId")
    List<Materia> obtenerPorUsuario(int userId);

    @Query("SELECT * FROM materias WHERE id = :materiaId")
    Materia obtenerPorId(int materiaId);
}

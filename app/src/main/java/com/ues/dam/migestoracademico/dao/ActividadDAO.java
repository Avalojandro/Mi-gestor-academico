package com.ues.dam.migestoracademico.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ues.dam.migestoracademico.entities.Actividad;

import java.util.List;

@Dao
public interface ActividadDAO {

    // Obtener actividades de una materia específica
    @Query("SELECT * FROM actividades WHERE materiaId = :materiaId")
    List<Actividad> obtenerPorMateria(int materiaId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Actividad actividad);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarTodas(List<Actividad> actividades);

    @Update
    void actualizar(Actividad actividad);

    @Delete
    void eliminar(Actividad actividad);

    // Para limpiar datos al sincronizar
    @Query("DELETE FROM actividades WHERE materiaId = :materiaId")
    void eliminarTodasDeMateria(int materiaId);
}
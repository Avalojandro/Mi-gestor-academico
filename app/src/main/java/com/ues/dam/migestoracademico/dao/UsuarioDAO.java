package com.ues.dam.migestoracademico.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ues.dam.migestoracademico.entities.Usuario;

@Dao
public interface UsuarioDAO {
    @Insert
    void crear(Usuario usuario);

    @Query("SELECT * FROM usuarios WHERE username = :username")
    Usuario buscarPorUsername(String username);

    @Query("SELECT COUNT(*) FROM usuarios WHERE username = :username")
    int existeUsuario(String username);
}
package com.ues.dam.migestoracademico.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ues.dam.migestoracademico.entities.Usuario;

@Dao
public interface UsuarioDAO {
    @Insert
    void crear(Usuario usuario);

    @Query("SELECT * FROM usuarios WHERE email = :email AND password = :password")
    Usuario login(String email, String password);

    @Query("SELECT * FROM usuarios WHERE email = :email")
    Usuario buscarPorEmail(String email);

    @Query("SELECT COUNT(*) FROM usuarios WHERE email = :email")
    int existeUsuario(String email);
}



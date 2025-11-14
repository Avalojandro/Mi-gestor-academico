package com.ues.dam.migestoracademico.repositories;


import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.ues.dam.migestoracademico.entities.Usuario;

public class UsuarioRepository {

    private static final String COLLECTION_NAME = "usuarios";

    private static CollectionReference getCollection() {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    public static Task<Void> saveUser(Usuario user) {
        return getCollection().document().set(user);
    }

    public static Task<QuerySnapshot> getUserByEmail(String email) {
        return getCollection()
                .whereEqualTo("email", email)
                .limit(1)
                .get();
    }

    public static Task<Void> updateUser(String documentId, Usuario user) {
        return getCollection().document(documentId).set(user);
    }

    public static Task<Void> deleteUser(String documentId) {
        return getCollection().document(documentId).delete();
    }
}
